"use client";

import { useEffect, useState } from "react";

export type MarkName =
  | "record"
  | "shield"
  | "clock"
  | "goods"
  | "service"
  | "sequence"
  | "empty"
  | "inbox"
  | "success"
  | "search";

const SRC: Record<MarkName, string> = {
  record: "/icons/record.json",
  shield: "/icons/shield.json",
  clock: "/icons/clock.json",
  goods: "/icons/goods.json",
  service: "/icons/service.json",
  sequence: "/icons/sequence.json",
  empty: "/icons/empty.json",
  inbox: "/icons/inbox.json",
  success: "/icons/success.json",
  search: "/icons/search.json",
};

let defining: Promise<void> | null = null;

function defineLordicon() {
  if (!defining) {
    defining = import("@lordicon/element").then(({ defineElement }) => {
      defineElement();
    });
  }
  return defining;
}

function palette(dark: boolean) {
  return dark ? "primary:#7fe08a,secondary:#ffffff" : "primary:#00681d,secondary:#191b17";
}

export function Mark({
  name,
  className = "size-16",
  dark = false,
  trigger = "hover",
}: {
  name: MarkName;
  className?: string;
  dark?: boolean;
  trigger?: "hover" | "in" | "loop-on-hover";
}) {
  const [motion, setMotion] = useState(true);
  const [ready, setReady] = useState(false);

  useEffect(() => {
    void defineLordicon().then(() => setReady(true));
    const query = window.matchMedia("(prefers-reduced-motion: reduce)");
    const sync = () => setMotion(!query.matches);
    sync();
    query.addEventListener("change", sync);
    return () => query.removeEventListener("change", sync);
  }, []);

  if (!ready) {
    return <span className={className} aria-hidden />;
  }

  return (
    <lord-icon
      src={SRC[name]}
      trigger={motion ? trigger : undefined}
      state={motion && trigger === "in" ? "in-reveal" : undefined}
      colors={palette(dark)}
      stroke="regular"
      className={className}
    />
  );
}

export function IllustrationWell({
  name,
  label,
  dark = false,
  size = "md",
}: {
  name: MarkName;
  label?: string;
  dark?: boolean;
  size?: "md" | "lg";
}) {
  const box = size === "lg" ? "size-44" : "size-[5.5rem]";
  const icon = size === "lg" ? "size-40" : "size-12";
  return (
    <div
      className={`grid ${box} place-items-center rounded-panel ${dark ? "bg-white/8" : "bg-primary-tint/70"}`}
      role="img"
      aria-label={label}
    >
      <Mark name={name} dark={dark} trigger="in" className={icon} />
    </div>
  );
}
