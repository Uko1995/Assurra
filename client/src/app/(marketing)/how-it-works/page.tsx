import type { Metadata } from "next";
import { PageHero } from "@/components/PageHero";
import { Button } from "@/components/ui/Button";
import { Section, SectionHeading } from "@/components/ui/Section";
import {
  howItWorksSteps,
  images,
  lifecycleNotes,
  resolutionCenter,
} from "@/content/pages";
import { appPath } from "@/lib/site";

export const metadata: Metadata = {
  title: "How it works",
  description:
    "Follow the Assurra escrow lifecycle from create and pay through ship, 72-hour confirm, and payout.",
};

export default function HowItWorksPage() {
  return (
    <>
      <PageHero
        eyebrow="How it works"
        title="Trust, secured. Transactions, simplified."
        description="Seven clear steps from payment to payout — including the 72-hour confirmation window, auto-release, and dispute freezes."
        image={images.hiwDispute}
        actions={<Button href={appPath("/register/customer")}>Start an escrow</Button>}
      />

      <Section>
        <SectionHeading
          title="Happy path"
          description="Statuses match the real Assurra state machine used by the API and dashboards."
        />
        <ol className="space-y-4">
          {howItWorksSteps.map((step, index) => (
            <li
              key={step.title}
              className="grid gap-4 border-b border-border-subtle pb-5 sm:grid-cols-[auto_1fr]"
            >
              <div className="flex h-12 w-12 items-center justify-center rounded-full bg-primary font-serif text-lg font-bold text-on-primary">
                {index + 1}
              </div>
              <div>
                <div className="flex flex-wrap items-center gap-3">
                  <h3 className="font-serif text-xl font-semibold text-on-surface">{step.title}</h3>
                  <span className="text-xs font-semibold uppercase tracking-wide text-primary">
                    {step.status}
                  </span>
                </div>
                <p className="mt-2 text-sm leading-7 text-muted sm:text-base">{step.body}</p>
              </div>
            </li>
          ))}
        </ol>
      </Section>

      <Section tone="muted">
        <SectionHeading
          title="Lifecycle notes"
          description="Timers and fee rules that keep both sides protected."
        />
        <div className="grid gap-8 md:grid-cols-3">
          {lifecycleNotes.map((note) => (
            <article key={note.title}>
              <h3 className="font-serif text-lg font-semibold text-on-surface">{note.title}</h3>
              <p className="mt-3 text-sm leading-7 text-muted">{note.body}</p>
            </article>
          ))}
        </div>
      </Section>

      <Section>
        <SectionHeading
          eyebrow="Resolution Center"
          title={resolutionCenter.title}
          description={resolutionCenter.description}
        />
        <ul className="space-y-3 text-sm leading-7 text-muted">
          {resolutionCenter.points.map((point) => (
            <li key={point} className="flex gap-2">
              <span className="mt-2 h-1.5 w-1.5 shrink-0 rounded-full bg-primary" />
              {point}
            </li>
          ))}
        </ul>
        <div className="mt-8">
          <Button href="/security" variant="secondary">
            Learn about mediation
          </Button>
        </div>
      </Section>
    </>
  );
}
