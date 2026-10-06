type IconProps = { className?: string };

function Glyph({ className = "size-4", children }: IconProps & { children: React.ReactNode }) {
  return (
    <svg
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth={1.75}
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
      className={className}
    >
      {children}
    </svg>
  );
}

export const icons = {
  record: (props: IconProps) => (
    <Glyph {...props}>
      <path d="M5 4h14v16l-3-2-2 2-2-2-2 2-3-2z" />
      <path d="M9 9h6M9 13h4" />
    </Glyph>
  ),
  plus: (props: IconProps) => (
    <Glyph {...props}>
      <path d="M12 5v14M5 12h14" />
    </Glyph>
  ),
  bell: (props: IconProps) => (
    <Glyph {...props}>
      <path d="M6 9a6 6 0 1 1 12 0c0 4 1.5 5.5 1.5 5.5h-15S6 13 6 9z" />
      <path d="M10 18a2 2 0 0 0 4 0" />
    </Glyph>
  ),
  shield: (props: IconProps) => (
    <Glyph {...props}>
      <path d="M12 3l7 3v6c0 4.5-3 7.5-7 9-4-1.5-7-4.5-7-9V6z" />
      <path d="M9 12l2 2 4-4" />
    </Glyph>
  ),
  bank: (props: IconProps) => (
    <Glyph {...props}>
      <path d="M4 10l8-5 8 5" />
      <path d="M6 10v8M10 10v8M14 10v8M18 10v8" />
      <path d="M3 20h18" />
    </Glyph>
  ),
  sliders: (props: IconProps) => (
    <Glyph {...props}>
      <path d="M5 7h14M5 12h14M5 17h14" />
      <circle cx="9" cy="7" r="2" />
      <circle cx="15" cy="12" r="2" />
      <circle cx="9" cy="17" r="2" />
    </Glyph>
  ),
  grid: (props: IconProps) => (
    <Glyph {...props}>
      <rect x="4" y="4" width="7" height="7" rx="1.5" />
      <rect x="13" y="4" width="7" height="7" rx="1.5" />
      <rect x="4" y="13" width="7" height="7" rx="1.5" />
      <rect x="13" y="13" width="7" height="7" rx="1.5" />
    </Glyph>
  ),
  layers: (props: IconProps) => (
    <Glyph {...props}>
      <path d="M12 4l8 4-8 4-8-4z" />
      <path d="M4 12l8 4 8-4" />
      <path d="M4 16l8 4 8-4" />
    </Glyph>
  ),
  flag: (props: IconProps) => (
    <Glyph {...props}>
      <path d="M6 4v16" />
      <path d="M6 5h11l-2 4 2 4H6z" />
    </Glyph>
  ),
  alert: (props: IconProps) => (
    <Glyph {...props}>
      <path d="M12 3l7 3v6c0 4.5-3 7.5-7 9-4-1.5-7-4.5-7-9V6z" />
      <path d="M12 9v4M12 16h.01" />
    </Glyph>
  ),
  percent: (props: IconProps) => (
    <Glyph {...props}>
      <path d="M19 5L5 19" />
      <circle cx="7.5" cy="7.5" r="2.5" />
      <circle cx="16.5" cy="16.5" r="2.5" />
    </Glyph>
  ),
  undo: (props: IconProps) => (
    <Glyph {...props}>
      <path d="M4 9h9a5 5 0 1 1 0 10H7" />
      <path d="M4 9l4-4M4 9l4 4" />
    </Glyph>
  ),
  chevron: (props: IconProps) => (
    <Glyph {...props}>
      <path d="M9 6l6 6-6 6" />
    </Glyph>
  ),
  check: (props: IconProps) => (
    <Glyph {...props}>
      <path d="M5 13l4 4L19 7" />
    </Glyph>
  ),
  clock: (props: IconProps) => (
    <Glyph {...props}>
      <circle cx="12" cy="12" r="8.5" />
      <path d="M12 8v4.5l3 1.8" />
    </Glyph>
  ),
  menu: (props: IconProps) => (
    <Glyph {...props}>
      <path d="M4 7h16M4 12h16M4 17h16" />
    </Glyph>
  ),
  close: (props: IconProps) => (
    <Glyph {...props}>
      <path d="M6 6l12 12M18 6L6 18" />
    </Glyph>
  ),
};

export type IconName = keyof typeof icons;

export function Icon({ name, className }: { name: IconName; className?: string }) {
  const Glyphed = icons[name];
  return <Glyphed className={className} />;
}
