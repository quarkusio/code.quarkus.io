import { Button, Dropdown, DropdownItem, DropdownToggle, InputGroup, DropdownPosition } from '@patternfly/react-core';
import React, { SetStateAction, useState } from 'react';
import { useHotkeys } from 'react-hotkeys-hook';
import { ExtensionsLoader } from './extensions-loader';
import './form.scss';
import { QuarkusProject } from './code-quarkus';
import { ExtensionEntry, ExtensionsPicker } from './pickers/extensions-picker';
import { InfoPicker } from './pickers/info-picker';
import copy from 'copy-to-clipboard';
import { ClipboardIcon } from '@patternfly/react-icons';

interface CodeQuarkusFormProps {
  project: QuarkusProject;
  setProject: React.Dispatch<SetStateAction<QuarkusProject>>;
  onSave: () => void;
}

export function CodeQuarkusForm(props: CodeQuarkusFormProps) {
  const [isMetadataValid, setIsMetadataValid] = useState(false);
  const [isOpen, setIsOpen] = useState(false);
  const setProject = props.setProject;
  const setMetadata = (metadata: any, isValid: boolean) => {
    setIsMetadataValid(isValid);
    setProject((prev) => ({ ...prev, metadata }));
  };
  const setExtensions = (value: { extensions: string[] }) => setProject((prev) => ({ ...prev, extensions: value.extensions }));
  const save = () => {
    if (isMetadataValid) {
      props.onSave();
    }
  };
  useHotkeys('alt+enter', save);
  const keyName = window.navigator.userAgent.toLowerCase().indexOf('mac') > -1 ? '⌥' : 'alt';
  const packageName = props.project.metadata.packageName || props.project.metadata.groupId;
  const createMvn = `mvn io.quarkus:quarkus-maven-plugin:0.26.1:create \\
-DprojectGroupId=${props.project.metadata.groupId} \\
-DprojectArtifactId=${props.project.metadata.artifactId} \\
-DclassName="${packageName}.ExampleResource" \\
-Dextensions="${props.project.extensions.join(',')}"`;
  const download = `curl -o ${props.project.metadata.artifactId}.zip "${props.project.generateProject()}"
unzip ${props.project.metadata.artifactId}.zip
rm -f ${props.project.metadata.artifactId}.zip`
  const copyToClipboard = (command: string) => {
    copy(command);
    setIsOpen(false);
  }
  return (
    <div className="code-quarkus-form">
      <div className="form-header-sticky-container">
        <div className="form-header responsive-container">
          <div className="project-info">
            <div className="title">
              <h3>Application Info</h3>
            </div>
            <InfoPicker value={props.project.metadata} isValid={isMetadataValid} onChange={setMetadata} />
          </div>
          <div className="generate-project">
            <InputGroup>
              <Button aria-label="Generate your application" isDisabled={!isMetadataValid} className="main generate-button" onClick={save}>Generate your application ({keyName} + ⏎)</Button>
              <Dropdown className="generate-button"
                isOpen={isOpen}
                position={DropdownPosition.right}
                toggle={
                  <DropdownToggle onToggle={() => setIsOpen(!isOpen)}></DropdownToggle>
                }>
                <DropdownItem key="buildTool" onClick={() => copyToClipboard(createMvn)}>
                  Copy Build Tool creation command to clipboard
                  <ClipboardIcon />
                </DropdownItem>
                <DropdownItem key="curl" onClick={() => copyToClipboard(download)}>
                  Copy CURL command to clipboard
                  <ClipboardIcon />
                </DropdownItem>
              </Dropdown>
            </InputGroup>
          </div>
        </div>
      </div>
      <div className="project-extensions responsive-container">
        <div className="title">
          <h3>Extensions</h3>
        </div>
        <ExtensionsLoader name="extensions">
          {extensions => (
            <ExtensionsPicker
              entries={extensions as ExtensionEntry[]}
              value={{ extensions: props.project.extensions }}
              onChange={setExtensions}
              placeholder="RESTEasy, Hibernate ORM, Web..."
            />
          )}
        </ExtensionsLoader>
      </div>

    </div>
  );
}