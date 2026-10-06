import Link from "next/link";
import { Icon } from "@/components/icons";
import { IllustrationWell } from "@/components/illustrations";
import { nairaParts, statusTone, type Tone } from "@/lib/format";

export function Logo({ href = "/" }: { href?: string }) {
  return (
    <Link href={href} className="inline-flex items-center gap-2 text-ink">
      <span className="grid size-6 place-items-center rounded-[7px] bg-primary text-[0.8125rem] font-bold text-white">
        A
      </span>
      <span className="text-[1.0625rem] font-semibold tracking-tight">Assurra</span>
    </Link>
  );
}

/* ---------------------------------------------------------------- actions */

const variants = {
  primary: "bg-primary text-white hover:bg-primary-deep",
  secondary: "border border-line-strong bg-surface text-ink hover:bg-subtle",
  ghost: "text-ink hover:bg-subtle",
  danger: "bg-danger text-white hover:brightness-95",
} as const;

const sizes = {
  sm: "h-9 px-3",
  md: "h-11 px-4",
} as const;

export function Button({
  children,
  type = "button",
  variant = "primary",
  size = "md",
  full = false,
  disabled,
  onClick,
}: {
  children: React.ReactNode;
  type?: "button" | "submit";
  variant?: keyof typeof variants;
  size?: keyof typeof sizes;
  full?: boolean;
  disabled?: boolean;
  onClick?: () => void;
}) {
  return (
    <button
      type={type}
      disabled={disabled}
      onClick={onClick}
      className={`inline-flex min-h-11 items-center justify-center gap-2 rounded-field text-sm font-semibold transition duration-200 disabled:cursor-not-allowed disabled:opacity-45 ${
        variants[variant]
      } ${sizes[size]} ${full ? "w-full" : ""}`}
    >
      {children}
    </button>
  );
}

const linkBase =
  "inline-flex min-h-11 items-center justify-center gap-2 rounded-field px-5 text-sm font-semibold transition duration-200";

export const btnPrimary = `${linkBase} ${variants.primary}`;
export const btnSecondary = `${linkBase} ${variants.secondary}`;
/** Kept for pages that still import the older name. */
export const btnGhost = btnSecondary;

export function TextLink({ href, children }: { href: string; children: React.ReactNode }) {
  return (
    <Link href={href} className="font-semibold text-primary underline decoration-primary-edge decoration-2 underline-offset-4 hover:decoration-primary">
      {children}
    </Link>
  );
}

/* ----------------------------------------------------------------- inputs */

export const inputClass =
  "w-full min-h-11 rounded-field border border-line-strong bg-surface px-3 py-2.5 text-[0.9375rem] text-ink transition duration-200 focus:border-primary";

export function Field({
  label,
  hint,
  children,
}: {
  label: string;
  hint?: string;
  children: React.ReactNode;
}) {
  return (
    <label className="block space-y-1.5">
      <span className="block text-[0.8125rem] font-semibold text-ink">{label}</span>
      {children}
      {hint && <span className="block text-[0.8125rem] text-faint">{hint}</span>}
    </label>
  );
}

export function Checkbox({
  name,
  required,
  children,
}: {
  name: string;
  required?: boolean;
  children: React.ReactNode;
}) {
  return (
    <label className="flex items-start gap-2.5 text-[0.9375rem] text-muted">
      <input
        type="checkbox"
        name={name}
        required={required}
        className="mt-1 size-4 shrink-0 accent-primary"
      />
      <span>{children}</span>
    </label>
  );
}

export function Segmented<T extends string>({
  options,
  value,
  onChange,
}: {
  options: { value: T; label: string }[];
  value: T;
  onChange: (next: T) => void;
}) {
  return (
    <div className="inline-flex w-full gap-1 rounded-field bg-subtle p-1">
      {options.map((option) => (
        <button
          key={option.value}
          type="button"
          onClick={() => onChange(option.value)}
          aria-pressed={value === option.value}
          className={`flex-1 rounded-[7px] px-3 py-1.5 text-sm font-semibold transition ${
            value === option.value ? "bg-surface text-ink shadow-card" : "text-muted hover:text-ink"
          }`}
        >
          {option.label}
        </button>
      ))}
    </div>
  );
}

/* --------------------------------------------------------------- surfaces */

export function Card({
  children,
  className = "",
}: {
  children: React.ReactNode;
  className?: string;
}) {
  return (
    <section className={`rounded-card border border-line bg-surface p-5 shadow-card ${className}`}>
      {children}
    </section>
  );
}

export function Panel({
  children,
  className = "",
}: {
  children: React.ReactNode;
  className?: string;
}) {
  return (
    <section className={`rounded-panel bg-panel p-6 text-white md:p-8 ${className}`}>
      {children}
    </section>
  );
}

export function SectionHead({
  title,
  description,
  action,
}: {
  title: string;
  description?: string;
  action?: React.ReactNode;
}) {
  return (
    <div className="flex flex-wrap items-end justify-between gap-3">
      <div>
        <h2 className="text-lg font-semibold tracking-tight">{title}</h2>
        {description && <p className="mt-1 max-w-xl text-[0.9375rem] text-muted">{description}</p>}
      </div>
      {action}
    </div>
  );
}

/* ------------------------------------------------------------------ rows */

/** One bordered container with hairline-divided rows, not a stack of cards. */
export function RowGroup({ children }: { children: React.ReactNode }) {
  return (
    <div className="divide-y divide-line overflow-hidden rounded-card border border-line bg-surface shadow-card">
      {children}
    </div>
  );
}

export function Row({
  href,
  title,
  sub,
  meta,
  right,
  children,
}: {
  href?: string;
  title: React.ReactNode;
  sub?: React.ReactNode;
  meta?: React.ReactNode;
  right?: React.ReactNode;
  children?: React.ReactNode;
}) {
  const body = (
    <div className="flex items-center gap-4 px-4 py-3.5 md:px-5">
      <div className="min-w-0 flex-1">
        <p className="truncate text-[0.9375rem] font-semibold text-ink">{title}</p>
        {sub && <p className="mt-0.5 text-[0.875rem] text-muted">{sub}</p>}
        {meta && <p className="meta mt-1.5 text-faint">{meta}</p>}
      </div>
      {right && <div className="flex shrink-0 flex-col items-end gap-1.5 text-right">{right}</div>}
      {href && <Icon name="chevron" className="size-4 shrink-0 text-faint" />}
    </div>
  );

  if (href) {
    return (
      <Link href={href} className="block transition hover:bg-subtle">
        {body}
      </Link>
    );
  }
  return (
    <div>
      {body}
      {children && <div className="border-t border-line bg-subtle/60 px-4 py-4 md:px-5">{children}</div>}
    </div>
  );
}

/* ------------------------------------------------------------------ money */

export function Money({
  value,
  whole = false,
  className = "",
}: {
  value: number | string | null | undefined;
  whole?: boolean;
  className?: string;
}) {
  const { symbol, digits } = nairaParts(value, whole);
  return (
    <span className={`num whitespace-nowrap ${className}`}>
      <span className="text-faint">{symbol}</span>
      {digits}
    </span>
  );
}

/** The one number a screen is about. */
export function Amount({ value, label }: { value: number | string | null | undefined; label?: string }) {
  return (
    <div>
      {label && <p className="meta text-faint">{label}</p>}
      <p className="mt-1 text-[2.125rem] font-semibold tracking-[-0.03em]">
        <Money value={value} whole />
      </p>
    </div>
  );
}

/* ----------------------------------------------------------------- status */

const chips: Record<Tone, string> = {
  success: "bg-success-tint text-success",
  info: "bg-info-tint text-info",
  warn: "bg-warn-tint text-warn",
  danger: "bg-danger-tint text-danger",
  quiet: "bg-quiet-tint text-muted",
};

export function StatusPill({
  value,
  label,
  tone,
}: {
  value: string;
  label?: string;
  tone?: Tone;
}) {
  return (
    <span
      className={`inline-flex items-center rounded-full px-2.5 py-1 text-[0.75rem] font-semibold ${
        chips[tone ?? statusTone(value)]
      }`}
    >
      {label ?? value.replaceAll("_", " ").toLowerCase().replace(/^./, (c) => c.toUpperCase())}
    </span>
  );
}

/** The raw enum, kept visible but quiet. */
export function Meta({ children }: { children: React.ReactNode }) {
  return <span className="meta text-faint">{children}</span>;
}

export function Alert({
  children,
  tone = "danger",
  action,
}: {
  children: React.ReactNode;
  tone?: Tone;
  action?: React.ReactNode;
}) {
  const edge: Record<Tone, string> = {
    success: "border-success/25",
    info: "border-info/25",
    warn: "border-warn/25",
    danger: "border-danger/25",
    quiet: "border-line",
  };
  return (
    <div className={`flex flex-wrap items-start justify-between gap-3 rounded-field border px-3 py-2.5 text-[0.9375rem] ${chips[tone]} ${edge[tone]}`}>
      <p className="min-w-0 flex-1">{children}</p>
      {action}
    </div>
  );
}

/* ----------------------------------------------------------- empty + wait */

export function Empty({
  title,
  action,
  children,
  mark = "empty",
}: {
  title?: string;
  action?: React.ReactNode;
  children?: React.ReactNode;
  mark?: "record" | "shield" | "clock" | "goods" | "service" | "sequence" | "empty" | "inbox" | "success" | "search";
}) {
  return (
    <div className="grid place-items-center rounded-card border border-line bg-surface px-6 py-12 text-center shadow-card">
      <IllustrationWell name={mark} label={title} />
      {title && <p className="mt-5 text-[1.0625rem] font-semibold text-ink">{title}</p>}
      {children && <p className="mt-1.5 max-w-sm text-[0.9375rem] text-muted">{children}</p>}
      {action && <div className="mt-5 flex justify-center">{action}</div>}
    </div>
  );
}

export function Skeleton({ className = "" }: { className?: string }) {
  return <span className={`block animate-pulse rounded bg-subtle ${className}`} />;
}

export function SkeletonRows({ count = 3 }: { count?: number }) {
  return (
    <RowGroup>
      {Array.from({ length: count }).map((_, index) => (
        <div key={index} className="flex items-center gap-4 px-4 py-4 md:px-5">
          <div className="flex-1 space-y-2">
            <Skeleton className="h-3.5 w-48" />
            <Skeleton className="h-3 w-32" />
          </div>
          <Skeleton className="h-3.5 w-20" />
        </div>
      ))}
    </RowGroup>
  );
}

/* ------------------------------------------------------------------ misc */

export function KeyValue({ rows }: { rows: { label: string; value: React.ReactNode }[] }) {
  return (
    <dl className="divide-y divide-line">
      {rows.map((row) => (
        <div key={row.label} className="flex items-baseline justify-between gap-4 py-2.5 first:pt-0 last:pb-0">
          <dt className="text-[0.875rem] text-muted">{row.label}</dt>
          <dd className="text-[0.9375rem] font-medium text-ink">{row.value}</dd>
        </div>
      ))}
    </dl>
  );
}

export function Eyebrow({ children }: { children: React.ReactNode }) {
  return <p className="meta text-primary">{children}</p>;
}
