import React, { useEffect, useState } from 'react';
import { useAnalytics } from '../../core/analytics';
import { CopyToClipboard } from '../../core/components';
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
  const context = { element: 'extension-more-dropdown', extension: props.id };
  
  const openGuide = () => {
    analytics.event('Click',{ label: 'open extension guide', ...context });
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
              eventContext={{ label: 'add extension with Quarkus CLI command', ...context }}
              content={addCliExt}
              tooltipPlacement="left" zIndex={201}
            >Copy the <b>CLI</b> command to add this extension</CopyToClipboard>
          </DropdownItem>
          {buildTool==='MAVEN' && (
            <DropdownItem key="maven" as={Button}>
              <CopyToClipboard id="more-copy-maven"
                eventContext={{ label: 'add extension with Maven command', ...context }}
                content={addMvnExt}
                tooltipPlacement="left" zIndex={201}
              >Copy the <b>Maven</b> command to add this extension</CopyToClipboard>
            </DropdownItem> )}
          {buildTool==='GRADLE' && (
            <DropdownItem key="gradle" as={Button}>
              <CopyToClipboard
                id="more-copy-gradle"
                eventContext={{ label: 'add extension with Gradle command', ...context }}
                content={addGradleExt} tooltipPlacement="left" zIndex={201}
              >Copy the  <b>Gradle</b> command to add this extension</CopyToClipboard>
            </DropdownItem>)}
          {buildTool==='MAVEN' && (
            <DropdownItem key="mavenDep" as={Button}>
              <CopyToClipboard
                  id="more-copy-maven-dep"
                  eventContext={{ label: 'pom.xml dependency snippet', ...context }}
                  content={pomDepXml}
                  tooltipPlacement="left" zIndex={201}
              >Copy the pom.xml <b>dependency</b> snippet</CopyToClipboard>
            </DropdownItem>)}
          {buildTool==='GRADLE' && (
            <DropdownItem key="gradleDep" as={Button}>
              <CopyToClipboard
                  id="more-copy-gradle-dep"
                  eventContext={{ label: 'build.gradle dependency snippet', ...context }}
                content={gradleDep}
                tooltipPlacement="left" zIndex={201}
              >Copy the build.gradle <b>dependency</b> snippet</CopyToClipboard>
            </DropdownItem>)}
          {buildTool==='GRADLE_KOTLIN_DSL' && (
            <DropdownItem key="gradleDepKot" as={Button}>
              <CopyToClipboard
                  id="more-copy-gradle-dep-kot"
                  eventContext={{ label: 'build.gradle.kts dependency snippet', ...context }}
                  content={gradleDepKot}
                  tooltipPlacement="left" zIndex={201}
              >Copy the build.gradle.kts <b>dependency</b> snippet</CopyToClipboard>
            </DropdownItem>
          )}
          {pomBomXml && buildTool==='MAVEN' && (
            <DropdownItem key="mavenBom" as={Button}>
              <CopyToClipboard
                  id="more-copy-maven-bom"
                  eventContext={{ label: 'pom.xml bom snippet', ...context }}
                  content={pomBomXml}
                  tooltipPlacement="left" zIndex={201}
              >Copy the pom.xml <b>bom</b> snippet</CopyToClipboard>
            </DropdownItem>
          )}
          {gradleBom && buildTool==='GRADLE' && (
            <DropdownItem key="gradleBom" as={Button}>
              <CopyToClipboard
                  id="more-copy-gradle-bom"
                  eventContext={{ label: 'build.gradle bom snippet', ...context }}
                  content={gradleBom}
                  tooltipPlacement="left" zIndex={201}
              >Copy the build.gradle <b>bom</b> snippet</CopyToClipboard>
            </DropdownItem>
          )}
          {gradleBomKot && buildTool==='GRADLE_KOTLIN_DSL' && (
            <DropdownItem key="gradleBomKot" as={Button}>
              <CopyToClipboard
                  id="more-copy-gradle-bom-kot"
                  eventContext={{ label: 'build.gradle.kts bom snippet', ...context }}
                  content={gradleBomKot}
                  tooltipPlacement="left" zIndex={201}
              >Copy the build.gradle.kts <b>bom</b> snippet</CopyToClipboard>
            </DropdownItem>)}
          <DropdownItem key="id" as={Button}>
            <CopyToClipboard
                id="more-copy-gav"
                eventContext={{ label: 'extension GAV', ...context }}
                content={gav}
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
