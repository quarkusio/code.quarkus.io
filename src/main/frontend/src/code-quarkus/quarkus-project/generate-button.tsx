import React, {MouseEventHandler, useState} from 'react';
import { useAnalytics } from '../../core/analytics';
import { Button, Dropdown, DropdownItem, DropdownPosition } from '@patternfly/react-core';
import { DownloadIcon, GithubIcon, ShareIcon } from '@patternfly/react-icons';
import { createOnGitHub, getProjectDownloadUrl, Target } from '../api/quarkus-project-utils';
import { QuarkusProject } from '../api/model';
import { useHotkeys } from 'react-hotkeys-hook';

export function GenerateButtonToggle(props: { onClick: MouseEventHandler<any>, isDisabled: boolean }) {
  const keyName = window.navigator.userAgent.toLowerCase().indexOf('mac') > -1 ? '⌥' : 'alt';
  return (
    <Button aria-label="Generate your application" isDisabled={props.isDisabled} className="generate-button" onClick={props.onClick}>Generate your application ({keyName} + ⏎)</Button>
  );
}

export function GenerateButton(props: { project: QuarkusProject, isProjectValid: boolean, generate: (target?: Target) => void, githubClientId?: string }) {
  const [isMoreOpen, setIsMoreOpen] = useState(false);
  const [, setIsMoreClosing] = useState(false);
  const analytics = useAnalytics();
  const closeMore = () => {
    setIsMoreClosing(true);
    setTimeout(() => {
      setIsMoreClosing(prev => {
        if (prev) {
          setIsMoreOpen(false);
        }
        return false;
      });
    }, 1000);
  };

  const openMore = () => {
    if (!props.isProjectValid) {
      return;
    }
    setIsMoreOpen(true);
    setIsMoreClosing(false);
  };

  const downloadZip = (e: any) => {
    e.preventDefault();
    props.generate();
  };

  const defaultGenerate = () => {
    props.generate();
  };

  const downloadUrl = getProjectDownloadUrl(props.project);
  const moreItems = [
    (
      <DropdownItem key="zip" onClick={downloadZip} href={downloadUrl} target="_blank" rel="noopener noreferrer">
        <DownloadIcon/> Download as a zip
      </DropdownItem>
    )
  ];

  if (props.githubClientId) {
    const githubClick = () => {
      analytics.event('Extension', 'Click "Create on Github"');
      createOnGitHub(props.project, props.githubClientId!);
    };

    moreItems.push(
      <DropdownItem key="github" onClick={githubClick}>
        <GithubIcon/> Push to GitHub
      </DropdownItem>
    );
  }
  moreItems.push(
    <DropdownItem key="share" onClick={() => props.generate(Target.SHARE)}>
      <ShareIcon /> Share
    </DropdownItem>
  );
  useHotkeys('alt+enter', defaultGenerate, [props.isProjectValid, props.generate]);

  return moreItems.length > 1 ? (
    <Dropdown
      isOpen={isMoreOpen}
      position={DropdownPosition.left}
      onMouseLeave={closeMore}
      onMouseEnter={openMore}
      toggle={(<GenerateButtonToggle onClick={defaultGenerate} isDisabled={!props.isProjectValid} />)}
      onClick={(e) => e.stopPropagation()}
      dropdownItems={moreItems}
    />
  ) : <GenerateButtonToggle onClick={defaultGenerate} isDisabled={!props.isProjectValid} />;
}
