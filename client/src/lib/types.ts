export type Role = "CUSTOMER" | "MERCHANT" | "ADMIN";

export type User = {
  id: string;
  email: string;
  fullName: string;
  phone?: string;
  role: Role;
  kycStatus?: string;
  emailVerified?: boolean;
};

export type AuthSession = {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
  tokenType: string;
  user: User;
};

export type PageResult<T> = {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
};

export type Escrow = {
  id: string;
  reference: string;
  customerId: string;
  merchantId: string;
  amount: number;
  escrowFee?: number;
  merchantAmount?: number;
  currency: string;
  status: string;
  productDescription: string;
  productQuantity?: number;
  agreedDeliveryDays?: number;
  paymentLink?: string;
  trackingNumber?: string;
  logisticsProvider?: string;
  createdAt?: string;
  /** Server-side clocks. The escrow service runs all three. */
  paymentExpiresAt?: string;
  confirmationDeadline?: string;
  autoReleaseAt?: string;
};

export type NotificationItem = {
  id: string;
  subject: string;
  body: string;
  status: string;
  createdAt?: string;
  readAt?: string | null;
};

export type MerchantProfile = {
  userId: string;
  email: string;
  fullName: string;
  phone?: string;
  kycStatus?: string;
  businessName?: string;
  businessType?: string;
  bankName?: string;
  bankAccountNumber?: string;
  apiKeyPrefix?: string;
  webhookUrl?: string;
  isVerified?: boolean;
  kycRejectionReason?: string;
};

export type ApiKeyInfo = {
  merchantId: string;
  businessName?: string;
  apiKeyPrefix?: string;
  isVerified?: boolean;
};

export type Payout = {
  id: string;
  reference: string;
  escrowReference: string;
  amount: number;
  fee?: number;
  netAmount?: number;
  currency: string;
  status: string;
  createdAt?: string;
};

export type Dispute = {
  id: string;
  reference: string;
  escrowReference: string;
  customerId: string;
  merchantId: string;
  reason: string;
  description: string;
  status: string;
  amountDisputed: number;
  createdAt?: string;
};

export type KycMerchant = {
  userId: string;
  email: string;
  fullName: string;
  phone?: string;
  businessName?: string;
  businessType?: string;
  bankName?: string;
  bankAccountNumber?: string;
  kycSubmittedAt?: string;
};

export type FeeConfig = {
  id: string;
  merchantId?: string | null;
  feeType: string;
  feeValue: number;
  minFee?: number;
  maxFee?: number;
  isActive?: boolean;
};

export type AmlAlert = {
  id: string;
  alertType: string;
  amount: number;
  currency: string;
  status: string;
  notes?: string | null;
  createdAt?: string;
};
