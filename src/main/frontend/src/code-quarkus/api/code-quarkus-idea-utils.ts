import customProtocolCheck from "custom-protocol-check";
import { Dispatch, SetStateAction } from "react";
import { IdeaSupportResult } from "../quarkus-project/quarkus-project-flow";

export const DEFAULT_IDEA = 'idea';

export const openIdeaIfSupport = (cloneUrl: string, setIdeaSupport: Dispatch<SetStateAction<IdeaSupportResult>>) => {
  const ideaProtocol = getIdeaProtocol(DEFAULT_IDEA, cloneUrl);

  customProtocolCheck(
    ideaProtocol,
    () => {
      setIdeaSupport({
        isSupported: false,
        gitURL: cloneUrl,
      });
    },
    () => {
      setIdeaSupport({
        isSupported: true,
        gitURL: cloneUrl,
      });
    }
  );
}

export const getIdeaProtocol = (toolTag: string, cloneUrl: string): string => {
  return `jetbrains://${toolTag}/checkout/git?checkout.repo=${cloneUrl}&idea.required.plugins.id=Git4Idea`;
}