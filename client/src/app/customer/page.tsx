"use client";

import Link from "next/link";
import { DashboardShell } from "@/components/dashboard-shell";
import { EscrowQueues } from "@/components/escrow-queues";
import { btnPrimary } from "@/components/ui";

export default function CustomerHomePage() {
  const newEscrow = (
    <Link href="/customer/escrow/new" className={btnPrimary}>
      New escrow
    </Link>
  );

  return (
    <DashboardShell
      role="CUSTOMER"
      title="Your escrows"
      lede="Records waiting on you come first."
      action={newEscrow}
    >
      <EscrowQueues
        role="CUSTOMER"
        basePath="/customer/escrow"
        endpoint="/api/v1/escrow"
        emptyAction={newEscrow}
      />
    </DashboardShell>
  );
}
