import React, { useEffect, useState } from 'react';
import { CopyToClipboard, useAnalytics } from '../../core';
import { ExtensionEntry } from './extensions-picker';
import { FaAngleDown, FaAngleUp, FaMap } from 'react-icons/fa';
import DropdownItem from 'react-bootstrap/DropdownItem';
import { Button, Dropdown } from 'react-bootstrap';

interface ExtensionMoreDropdownProps extends ExtensionEntry {
  active: boolean;
}

export function ExtensionMoreDropdown(props: ExtensionMoreDropdownProps) {
  const [ isOpen , setIsOpen ] = useState(false);
  const analytics = useAnalytics();
  const gav = `${props.id}:${props.version}`;
  const gavArray = gav.split(':');
  const addMvnExt = `./mvnw quarkus:add-extension -Dextensions="${props.id}"`;
  const addGradleExt = `./gradlew addExtension --extensions="${props.id}"`;
  const xml = `        <dependency>\n            <groupId>${gavArray[0]}</groupId>\n            <artifactId>${gavArray[1]}</artifactId>\n        </dependency>`;

  const openGuide = () => {
    analytics.event('Extension', 'Click "Open Extension Guide" link', props.id);
  };

  useEffect(() => {
    if(!props.active && isOpen) {
      setIsOpen(false);
    }
  }, [ props.active, isOpen, setIsOpen ]);

  return (
    <Dropdown onClick={(e) => e.stopPropagation()} onToggle={setIsOpen} show={isOpen}>
      <Dropdown.Toggle className="extension-more-button">
        {isOpen ? <FaAngleUp /> : <FaAngleDown />}
      </Dropdown.Toggle>
      {props.active && (
        <Dropdown.Menu
          align="right"
          onClick={(e) => e.stopPropagation()}
        >
          <DropdownItem key="maven" as={Button}>
            <CopyToClipboard id="more-copy-maven"
              event={[ 'Extension', 'Maven quarkus:add-extension command', props.id ]}
              content={addMvnExt}
              tooltipPlacement="left" zIndex={201}
            >Maven quarkus:add-extension command</CopyToClipboard>
          </DropdownItem>
          <DropdownItem key="gradle" as={Button}>
            <CopyToClipboard id="more-copy-gradle"
              event={[ 'Extension', 'Gradle addExtension command', props.id ]}
              content={addGradleExt} tooltipPlacement="left" zIndex={201}
            >Gradle addExtension command</CopyToClipboard>
          </DropdownItem>
          <DropdownItem key="xml" as={Button}>
            <CopyToClipboard id="more-copy-pom" event={[ 'Extension', 'Copy the extension pom.xml snippet', props.id ]}
              content={xml}
              tooltipPlacement="left" zIndex={201}
            >pom.xml snippet</CopyToClipboard>
          </DropdownItem>
          <DropdownItem key="id" as={Button}>
            <CopyToClipboard id="more-copy-gav" event={[ 'Extension', 'Copy the GAV', props.id ]} content={gav}
              tooltipPlacement="left"
              zIndex={201}>GAV</CopyToClipboard>
          </DropdownItem>
          {props.guide && (<DropdownItem key="guide" href={props.guide} target="_blank" onClick={openGuide}>
            <FaMap/> Open Extension Guide
          </DropdownItem>)}
        </Dropdown.Menu>)
      }
    </Dropdown>

  );
}
