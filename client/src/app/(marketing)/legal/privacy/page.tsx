import { Band } from "@/components/marketing";
import { Eyebrow } from "@/components/ui";

export default function PrivacyPage() {
  return (
    <Band tight>
      <article className="max-w-2xl">
        <Eyebrow>Legal</Eyebrow>
        <h1 className="page-title mt-3">Privacy</h1>
        <div className="mt-8 space-y-4 border-t border-line pt-8 text-[1.0625rem] leading-7 text-muted">
          <p>
            Assurra collects the account details needed to run an escrow: your name, email, phone, and, for merchants,
            business and settlement information. Payments and KYC documents are stored so a record can be funded, paid
            out, or reviewed.
          </p>
          <p>
            We use that information to authenticate you, move an escrow through its statuses, notify the other party,
            and meet financial-record duties. Bank account numbers, BVNs, and phone numbers are stored encrypted. We do
            not sell personal information.
          </p>
          <p>
            You can ask for a copy of your data or to close your account once you are signed in. Marketing email is
            optional and can be turned off.
          </p>
        </div>
      </article>
    </Band>
  );
}
