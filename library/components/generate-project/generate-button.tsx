import React, { MouseEventHandler, useState } from 'react';
import { useAnalytics } from '@quarkusio/code-quarkus.core.analytics';
import { createOnGitHub, getProjectDownloadUrl, Target } from '../api/quarkus-project-utils';
import { QuarkusProject } from '../api/model';
import { useHotkeys } from 'react-hotkeys-hook';
import { Button, ButtonGroup, Dropdown } from 'react-bootstrap';
import { FaAngleDown, FaAngleUp, FaDownload, FaGithub } from 'react-icons/fa';
import './generate-button.scss';
import DropdownToggle from 'react-bootstrap/DropdownToggle';
import { Api } from '../api/code-quarkus-api';

export function GenerateButtonPrimary(props: { onClick: MouseEventHandler<any>, disabled: boolean }) {
  const keyName = window.navigator.userAgent.toLowerCase().indexOf('mac') > -1 ? '⌥' : 'alt';
  return (
    <Button aria-label="Generate your application" disabled={props.disabled} className="generate-button" onClick={props.onClick}>Generate your application ({keyName} + ⏎)</Button>
  );
}

export function GenerateButton(props: { api: Api, project: QuarkusProject, isProjectValid: boolean, generate: (target?: Target) => void, githubClientId?: string }) {
  const [ isMoreOpen , setIsMoreOpen ] = useState(false);
  const analytics = useAnalytics();

  function onMouseEnterFn(e) {
    onToggleFn(true);
  }

  function onMouseLeaveFn(e) {
    onToggleFn(false);
  }

  function onToggleFn(isOpen: boolean) {
    setIsMoreOpen(isOpen);
  }

  const downloadZip = (e: any) => {
    e.preventDefault();
    props.generate(Target.DOWNLOAD);
  };

  const defaultGenerate = () => {
    props.generate(Target.GENERATE);
  };

  const downloadUrl = getProjectDownloadUrl(props.api, props.project);

  const githubClick = () => {
    analytics.event('Extension', 'Click "Create on Github"');
    createOnGitHub(props.api, props.project, props.githubClientId!);
  };
  useHotkeys('alt+enter', defaultGenerate, [ props.isProjectValid, props.generate ]);

  return (
    <Dropdown as={ButtonGroup} show={isMoreOpen} onMouseLeave={onMouseLeaveFn} className="generate-project-button">
      <GenerateButtonPrimary onClick={defaultGenerate} disabled={!props.isProjectValid} />
      <DropdownToggle className="generate-button-split-more" disabled={!props.isProjectValid} onMouseEnter={onMouseEnterFn} >
        {isMoreOpen ? <FaAngleUp /> : <FaAngleDown />}
      </DropdownToggle>
      <Dropdown.Menu onMouseEnter={onMouseEnterFn} align="right">
        <Dropdown.Item as={Button} key="zip" onClick={downloadZip} href={downloadUrl} target="_blank" rel="noopener noreferrer">
          <FaDownload/> Download as a zip
        </Dropdown.Item>
        {props.githubClientId && (
          <Dropdown.Item as={Button} key="github" onClick={githubClick}>
            <FaGithub/> Push to GitHub
          </Dropdown.Item>
        )}
      </Dropdown.Menu>
    </Dropdown>
  );
}
