export const FEE_RATE = 0.015;
export const FEE_MIN = 500;
export const FEE_MAX = 50_000;
export const ESCROW_MAX = 10_000_000;
export const STAGE_DAYS_MAX = 30;

export function naira(value: number | string | null | undefined) {
  const raw = typeof value === "string" ? Number(value) : value ?? 0;
  const amount = Number.isFinite(raw) ? raw : 0;
  return new Intl.NumberFormat("en-NG", {
    style: "currency",
    currency: "NGN",
    // Kobo only when there is kobo, so prose reads ₦50,000 rather than ₦50,000.00.
    minimumFractionDigits: Number.isInteger(amount) ? 0 : 2,
    maximumFractionDigits: 2,
  }).format(amount);
}

/** The symbol and the digits separately, so the symbol can be set quieter. */
export function nairaParts(value: number | string | null | undefined, whole = false) {
  const raw = typeof value === "string" ? Number(value) : value ?? 0;
  const amount = Number.isFinite(raw) ? raw : 0;
  return {
    symbol: "₦",
    digits: new Intl.NumberFormat("en-NG", {
      minimumFractionDigits: whole ? 0 : 2,
      maximumFractionDigits: whole ? 0 : 2,
    }).format(amount),
  };
}

export function shortDateTime(iso: string | null | undefined) {
  if (!iso) return null;
  const date = new Date(iso);
  if (Number.isNaN(date.getTime())) return null;
  return new Intl.DateTimeFormat("en-NG", {
    day: "numeric",
    month: "short",
    hour: "2-digit",
    minute: "2-digit",
  }).format(date);
}

/** "2 days left", "4 hours left", or "overdue". Null when the date is unusable. */
export function timeLeft(iso: string | null | undefined) {
  if (!iso) return null;
  const target = new Date(iso).getTime();
  if (Number.isNaN(target)) return null;
  const minutes = Math.round((target - Date.now()) / 60000);
  if (minutes <= 0) return "overdue";
  if (minutes < 60) return `${minutes} min left`;
  const hours = Math.round(minutes / 60);
  if (hours < 48) return `${hours} ${hours === 1 ? "hour" : "hours"} left`;
  return `${Math.round(hours / 24)} days left`;
}

export function escrowFee(amount: number) {
  if (!Number.isFinite(amount) || amount <= 0) return 0;
  return Math.min(FEE_MAX, Math.max(FEE_MIN, Math.round(amount * FEE_RATE * 100) / 100));
}

export function homeForRole(role: string) {
  if (role === "ADMIN") return "/admin";
  if (role === "MERCHANT") return "/merchant";
  return "/customer";
}

export function labelize(value: string) {
  return value
    .toLowerCase()
    .split("_")
    .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
    .join(" ");
}

const CUSTOMER_STATUS: Record<string, string> = {
  INITIATED: "Waiting on you to pay",
  FUNDED: "Waiting on the merchant to start",
  MERCHANT_NOTIFIED: "Waiting on the merchant to start",
  SHIPPED: "This stage is in progress",
  DELIVERED: "Waiting on you to confirm",
  CONFIRMED: "Confirmed. Release follows this status.",
  RELEASED: "Released to the merchant",
  AUTO_RELEASED: "Released to the merchant",
  DISPUTED: "In dispute",
  UNDER_REVIEW: "In review",
  CANCELLED: "Cancelled",
  REFUNDED: "Refunded",
  RESOLVED_CUSTOMER: "Resolved in your favour",
  RESOLVED_MERCHANT: "Resolved for the merchant",
};

const MERCHANT_STATUS: Record<string, string> = {
  INITIATED: "Waiting on the payer to pay",
  FUNDED: "Waiting on you to start this stage",
  MERCHANT_NOTIFIED: "Waiting on you to start this stage",
  SHIPPED: "Waiting on you to mark this stage complete",
  DELIVERED: "Waiting on the payer to confirm",
  CONFIRMED: "Payer confirmed. Release follows this status.",
  RELEASED: "Released to you",
  AUTO_RELEASED: "Released to you",
  DISPUTED: "The payer opened a dispute",
  UNDER_REVIEW: "In review",
  CANCELLED: "Cancelled",
  REFUNDED: "Refunded to the payer",
  RESOLVED_CUSTOMER: "Resolved for the payer",
  RESOLVED_MERCHANT: "Resolved for you",
};

const ADMIN_STATUS: Record<string, string> = {
  INITIATED: "Waiting on the payer to pay",
  FUNDED: "Waiting on the merchant to start",
  MERCHANT_NOTIFIED: "Waiting on the merchant to start",
  SHIPPED: "In progress, not yet marked complete",
  DELIVERED: "Waiting on the payer to confirm",
  CONFIRMED: "Confirmed, not yet released",
  RELEASED: "Released",
  AUTO_RELEASED: "Released",
  DISPUTED: "In dispute",
  UNDER_REVIEW: "In review",
  CANCELLED: "Cancelled",
  REFUNDED: "Refunded",
  RESOLVED_CUSTOMER: "Resolved for the payer",
  RESOLVED_MERCHANT: "Resolved for the merchant",
};

export function statusSentence(status: string, role: "CUSTOMER" | "MERCHANT" | "ADMIN" = "ADMIN") {
  const table = role === "CUSTOMER" ? CUSTOMER_STATUS : role === "MERCHANT" ? MERCHANT_STATUS : ADMIN_STATUS;
  return table[status] ?? labelize(status);
}

const CLOSED = new Set([
  "RELEASED",
  "AUTO_RELEASED",
  "CANCELLED",
  "REFUNDED",
  "RESOLVED_CUSTOMER",
  "RESOLVED_MERCHANT",
]);

export function escrowBucket(role: "CUSTOMER" | "MERCHANT", status: string): "needs" | "progress" | "closed" {
  if (CLOSED.has(status)) return "closed";
  if (role === "CUSTOMER" && (status === "INITIATED" || status === "DELIVERED")) return "needs";
  if (role === "MERCHANT" && (status === "FUNDED" || status === "MERCHANT_NOTIFIED" || status === "SHIPPED")) return "needs";
  return "progress";
}

export type Tone = "success" | "info" | "warn" | "danger" | "quiet";

/**
 * One tone per status across escrows, payments, payouts, disputes and KYC.
 * warn means a person has to act, info means it is moving, quiet means it
 * ended without a release. Pages with a clashing enum pass `tone` instead.
 */
const TONES: Record<string, Tone> = {
  INITIATED: "warn",
  FUNDED: "info",
  MERCHANT_NOTIFIED: "info",
  SHIPPED: "info",
  DELIVERED: "warn",
  CONFIRMED: "success",
  RELEASED: "success",
  AUTO_RELEASED: "success",
  DISPUTED: "danger",
  UNDER_REVIEW: "warn",
  RESOLVED_CUSTOMER: "success",
  RESOLVED_MERCHANT: "success",
  REFUNDED: "quiet",
  CANCELLED: "quiet",
  PENDING: "warn",
  QUEUED: "info",
  PROCESSING: "info",
  SUCCESS: "success",
  COMPLETED: "success",
  FAILED: "danger",
  REVERSED: "danger",
  OPEN: "danger",
  CLOSED: "quiet",
  APPROVED: "success",
  VERIFIED: "success",
  REJECTED: "danger",
  NOT_SUBMITTED: "quiet",
  CLEARED: "success",
  ESCALATED: "warn",
};

export function statusTone(status: string): Tone {
  return TONES[status] ?? "quiet";
}

export function nextActionLabel(role: "CUSTOMER" | "MERCHANT", status: string) {
  if (role === "CUSTOMER" && status === "INITIATED") return "Pay";
  if (role === "CUSTOMER" && status === "DELIVERED") return "Confirm";
  if (role === "MERCHANT" && (status === "FUNDED" || status === "MERCHANT_NOTIFIED")) return "Record progress";
  if (role === "MERCHANT" && status === "SHIPPED") return "Mark complete";
  return null;
}
