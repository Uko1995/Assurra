import type { CSSProperties, HTMLAttributes } from "react";

declare module "react" {
  namespace JSX {
    interface IntrinsicElements {
      "lord-icon": HTMLAttributes<HTMLElement> & {
        src?: string;
        trigger?: string;
        colors?: string;
        stroke?: string;
        state?: string;
        target?: string;
        loading?: string;
        style?: CSSProperties;
      };
    }
  }
}
