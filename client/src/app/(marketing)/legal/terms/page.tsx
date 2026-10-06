import { Band } from "@/components/marketing";
import { Eyebrow } from "@/components/ui";

export default function TermsPage() {
  return (
    <Band tight>
      <article className="max-w-2xl">
        <Eyebrow>Legal</Eyebrow>
        <h1 className="page-title mt-3">Terms</h1>
        <div className="mt-8 space-y-4 border-t border-line pt-8 text-[1.0625rem] leading-7 text-muted">
          <p>
            Assurra provides escrow for a transaction between a payer and a merchant. Creating an account means you
            accept these terms and consent to the processing required to hold and release funds.
          </p>
          <p>
            The published fee is 1.5 percent of the escrow amount, with a floor of ₦500 and a cap of ₦50,000, unless a
            custom schedule has been set for the account. Amounts are stated in naira. One record holds ₦100 to
            ₦10,000,000 over a window of 1 to 30 days.
          </p>
          <p>
            The payer confirms the stage to release a payout. If the confirmation window of 72 hours passes without an
            answer, the escrow auto-releases to the merchant. Disputes are decided from the evidence on the record.
            Merchants must pass KYC before they can receive settlement.
          </p>
        </div>
      </article>
    </Band>
  );
}
