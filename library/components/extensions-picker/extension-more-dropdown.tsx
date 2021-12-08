import React, { useEffect, useState } from 'react';
import { useAnalytics } from '@quarkusio/code-quarkus.core.analytics';
import { CopyToClipboard } from '@quarkusio/code-quarkus.core.components';
import { ExtensionEntry } from './extensions-picker';
import { FaAngleDown, FaAngleUp, FaMap } from 'react-icons/fa';
import DropdownItem from 'react-bootstrap/DropdownItem';
import { Button, Dropdown } from 'react-bootstrap';

interface ExtensionMoreDropdownProps extends ExtensionEntry {
  active: boolean;
  buildTool?: string;
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
  const gradleDep = `implementation "${props.platform ? props.id : gav}"`;
  const gradleBom = `implementation enforcedPlatform("${props.bom}")`;
  const gradleDepKot = `implementation("${props.platform ? props.id : gav}")`;
  const gradleBomKot = `implementation(enforcedPlatform("${props.bom}"))`;
  const pomDepXml = `        <dependency>\n            <groupId>${gavArray[0]}</groupId>\n            <artifactId>${gavArray[1]}</artifactId>\n${props.platform ? '' : `            <version>${props.version}</version>\n`}        </dependency>`;
  const pomBomXml = bomArray && `        <dependency>\n            <groupId>${bomArray[0]}</groupId>\n            <artifactId>${bomArray[1]}</artifactId>\n            <version>${bomArray[2]}</version>\n            <type>pom</type>\n            <scope>import</scope>\n        </dependency>`;
  const buildTool = props.buildTool;
  
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
            >Copy the <b>CLI</b> command to add this extension</CopyToClipboard>
          </DropdownItem>
          {buildTool==='MAVEN' && (
            <DropdownItem key="maven" as={Button}>
              <CopyToClipboard id="more-copy-maven"
                event={[ 'Extension', 'Copy the maven add extension command', props.id ]}
                content={addMvnExt}
                tooltipPlacement="left" zIndex={201}
              >Copy the <b>Maven</b> command to add this extension</CopyToClipboard>
            </DropdownItem> )}
          {buildTool==='GRADLE' && (
            <DropdownItem key="gradle" as={Button}>
              <CopyToClipboard id="more-copy-gradle"
                event={[ 'Extension', 'Copy the gradle add extension command', props.id ]}
                content={addGradleExt} tooltipPlacement="left" zIndex={201}
              >Copy the  <b>Gradle</b> command to add this extension</CopyToClipboard>
            </DropdownItem>)}
          {buildTool==='MAVEN' && (
            <DropdownItem key="mavenDep" as={Button}>
              <CopyToClipboard id="more-copy-maven-dep" event={[ 'Extension', 'Copy the pom.xml dependency snippet', props.id ]}
                content={pomDepXml}
                tooltipPlacement="left" zIndex={201}
              >Copy the pom.xml <b>dependency</b> snippet</CopyToClipboard>
            </DropdownItem>)}
          {buildTool==='GRADLE' && (
            <DropdownItem key="gradleDep" as={Button}>
              <CopyToClipboard id="more-copy-gradle-dep" event={[ 'Extension', 'Copy the build.gradle dependency snippet', props.id ]}
                content={gradleDep}
                tooltipPlacement="left" zIndex={201}
              >Copy the build.gradle <b>dependency</b> snippet</CopyToClipboard>
            </DropdownItem>)}
          {buildTool==='GRADLE_KOTLIN_DSL' && (
            <DropdownItem key="gradleDepKot" as={Button}>
              <CopyToClipboard id="more-copy-gradle-dep-kot" event={[ 'Extension', 'Copy the build.gradle.kts dependency snippet', props.id ]}
                content={gradleDepKot}
                tooltipPlacement="left" zIndex={201}
              >Copy the build.gradle.kts <b>dependency</b> snippet</CopyToClipboard>
            </DropdownItem>
          )}
          {pomBomXml && buildTool==='MAVEN' && (
            <DropdownItem key="mavenBom" as={Button}>
              <CopyToClipboard id="more-copy-maven-bom" event={[ 'Extension', 'Copy the pom.xml bom snippet', props.id ]}
                content={pomBomXml}
                tooltipPlacement="left" zIndex={201}
              >Copy the pom.xml <b>bom</b> snippet</CopyToClipboard>
            </DropdownItem>
          )}
          {gradleBom && buildTool==='GRADLE' && (
            <DropdownItem key="gradleBom" as={Button}>
              <CopyToClipboard id="more-copy-gradle-bom" event={[ 'Extension', 'Copy the build.gradle bom snippet', props.id ]}
                content={gradleBom}
                tooltipPlacement="left" zIndex={201}
              >Copy the build.gradle <b>bom</b> snippet</CopyToClipboard>
            </DropdownItem>
          )}
          {gradleBomKot && buildTool==='GRADLE_KOTLIN_DSL' && (
            <DropdownItem key="gradleBomKot" as={Button}>
              <CopyToClipboard id="more-copy-gradle-bom-kot" event={[ 'Extension', 'Copy the build.gradle.kts bom snippet', props.id ]}
                content={gradleBomKot}
                tooltipPlacement="left" zIndex={201}
              >Copy the build.gradle.kts <b>bom</b> snippet</CopyToClipboard>
            </DropdownItem>)}
          <DropdownItem key="id" as={Button}>
            <CopyToClipboard id="more-copy-gav" event={[ 'Extension', 'Copy the extension GAV', props.id ]} content={gav}
              tooltipPlacement="left"
              zIndex={201}>Copy the <b>groupId:artifactId:version</b></CopyToClipboard>
          </DropdownItem>
          {props.guide && (<DropdownItem key="guide" href={props.guide} target="_blank" onClick={openGuide}>
            <FaMap/> See the extension&nbsp;<b>guide</b>
          </DropdownItem>)}
        </Dropdown.Menu>)
      }
    </Dropdown>

  );
}
