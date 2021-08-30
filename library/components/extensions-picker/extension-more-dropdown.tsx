import React, { useEffect, useState } from 'react';
import { useAnalytics } from '@quarkusio/code-quarkus.core.analytics';
import { CopyToClipboard } from '@quarkusio/code-quarkus.core.components';
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
  const bomArray = props.bom && props.bom.split(':');
  const addMvnExt = `./mvnw quarkus:add-extension -Dextensions="${props.id}"`;
  const addCliExt = `quarkus ext add ${props.id}`;
  const addGradleExt = `./gradlew addExtension --extensions="${props.id}"`;
  const pomDepXml = `        <dependency>\n            <groupId>${gavArray[0]}</groupId>\n            <artifactId>${gavArray[1]}</artifactId>\n        </dependency>`;
  const pomBomXml = bomArray && `        <dependency>\n            <groupId>${bomArray[0]}</groupId>\n            <artifactId>${bomArray[1]}</artifactId>\n            <version>${bomArray[2]}</version>\n            <type>pom</type>\n            <scope>import</scope>\n        </dependency>`;

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
          <DropdownItem key="cli" as={Button}>
            <CopyToClipboard id="more-copy-cli"
              event={[ 'Extension', 'Copy the Quarkus CLI add extension command', props.id ]}
              content={addCliExt}
              tooltipPlacement="left" zIndex={201}
            >Add it to your project with the Quarkus CLI</CopyToClipboard>
          </DropdownItem>
          <DropdownItem key="maven" as={Button}>
            <CopyToClipboard id="more-copy-maven"
              event={[ 'Extension', 'Copy the Maven add extension command', props.id ]}
              content={addMvnExt}
              tooltipPlacement="left" zIndex={201}
            >Add it to your project with Maven</CopyToClipboard>
          </DropdownItem>
          <DropdownItem key="gradle" as={Button}>
            <CopyToClipboard id="more-copy-gradle"
              event={[ 'Extension', 'Copy the Gradle add extension command', props.id ]}
              content={addGradleExt} tooltipPlacement="left" zIndex={201}
            >Add it to your project with Gradle</CopyToClipboard>
          </DropdownItem>
          <DropdownItem key="xml" as={Button}>
            <CopyToClipboard id="more-copy-pom" event={[ 'Extension', 'Copy the Maven pom.xml dependency snippet', props.id ]}
              content={pomDepXml}
              tooltipPlacement="left" zIndex={201}
            >Get the Maven pom.xml dependency snippet</CopyToClipboard>
          </DropdownItem>
          {pomBomXml && (
            <DropdownItem key="xml" as={Button}>
              <CopyToClipboard id="more-copy-pom" event={[ 'Extension', 'Copy the Maven pom.xml bom snippet', props.id ]}
                content={pomBomXml}
                tooltipPlacement="left" zIndex={201}
              >Get the Maven pom.xml bom snippet</CopyToClipboard>
            </DropdownItem>
          )}          
          <DropdownItem key="id" as={Button}>
            <CopyToClipboard id="more-copy-gav" event={[ 'Extension', 'Copy the extension GAV', props.id ]} content={gav}
              tooltipPlacement="left"
              zIndex={201}>Get the groupId:artifactId:version</CopyToClipboard>
          </DropdownItem>
          {props.guide && (<DropdownItem key="guide" href={props.guide} target="_blank" onClick={openGuide}>
            <FaMap/> See the extension guide
          </DropdownItem>)}
        </Dropdown.Menu>)
      }
    </Dropdown>

  );
}
