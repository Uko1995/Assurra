import type { Preview } from "@storybook/react";
import { Inter, Fraunces } from "next/font/google";
import "../src/shared/styles/globals.css";

const inter = Inter({
  subsets: ["latin"],
  variable: "--font-inter",
  display: "swap",
});

const fraunces = Fraunces({
  subsets: ["latin"],
  variable: "--font-display",
  display: "swap",
});

const preview: Preview = {
  parameters: {
    controls: {
      matchers: {
        color: /(background|color)$/i,
        date: /Date$/i,
      },
    },
    viewport: {
      defaultViewport: "mobile1",
    },
    backgrounds: {
      default: "light",
      values: [
        { name: "light", value: "#F6F8F6" },
        { name: "surface", value: "#FFFFFF" },
        { name: "deep", value: "#0A3D16" },
        { name: "dark", value: "#0A0F0B" },
      ],
    },
  },
  decorators: [
    (Story) => (
      <div className={`${inter.variable} ${fraunces.variable} font-sans antialiased`}>
        <Story />
      </div>
    ),
  ],
};

export default preview;
