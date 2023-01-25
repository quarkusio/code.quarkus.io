import React, { MouseEventHandler, useState } from 'react';
import { useAnalytics } from '@quarkusio/code-quarkus.core.analytics';
import { createOnGitHub, getProjectDownloadUrl, Target, existsStoredProject } from '../api/quarkus-project-utils';
import { QuarkusProject } from '../api/model';
import { useHotkeys } from 'react-hotkeys-hook';
import { Button, ButtonGroup, Dropdown } from 'react-bootstrap';
import { FaAngleDown, FaAngleUp, FaDownload, FaGithub, FaBookmark, FaCheck, FaTrashAlt } from 'react-icons/fa';
import './generate-button.scss';
import DropdownToggle from 'react-bootstrap/DropdownToggle';
import { Api } from '../api/code-quarkus-api';

export function GenerateButtonPrimary(props: { onClick: MouseEventHandler<any>, disabled: boolean }) {
  const keyName = window.navigator.userAgent.toLowerCase().indexOf('mac') > -1 ? '⌥' : 'alt';
  return (
    <Button aria-label="Generate your application" disabled={props.disabled} className="generate-button" onClick={props.onClick}>Generate your application ({keyName} + ⏎)</Button>
  );
}

export function GenerateButton(props: { api: Api, project: QuarkusProject, isProjectValid: boolean, generate: (target?: Target) => void, githubClientId?: string, isConfigSaved: boolean, storeAppConfig: () => void, resetAppConfig: () => void }) {

  const [ isMoreOpen , setIsMoreOpen ] = useState(false);
  const [ isResetEnabled, setResetEnabled] = useState(existsStoredProject());

  const analytics = useAnalytics();

  function onMouseEnterFn(e) {
    onToggleFn(true);
  }

  function onMouseLeaveFn(e) {
    onToggleFn(false);
  }

  function onToggleFn(isOpen: boolean) {
    setIsMoreOpen(isOpen);
    analytics.event('Generate app dropdown', { action: isOpen ? 'close': 'open', element: 'generate-button' });
  }
  
  function handleStoreAppConfig() {
    props.storeAppConfig();
    setResetEnabled(true);
    analytics.event('Store app config', { element: 'generate-button' });
  }

  function resetStoredAppConfig() {
    props.resetAppConfig();
    setResetEnabled(false);
    analytics.event('Reset app config', { element: 'generate-button' });
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
    analytics.event('Click', { label: 'Create on Github', element: 'generate-button' });
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
        <Dropdown.Item as={Button} key="store" 
          aria-label="Store current app as default">
          <div className="store-app-config">
            <span className="store-config-button" onClick={handleStoreAppConfig}>
              { props.isConfigSaved ? <FaCheck /> : <FaBookmark /> }
              Store current app as default  
            </span>
            
            <div role="button" className="reset-config-button" onClick={resetStoredAppConfig}>
              { isResetEnabled && <FaTrashAlt className="reset-to-default"/> }
            </div>
           </div>
        </Dropdown.Item>
      </Dropdown.Menu>
    </Dropdown>
  );
}
