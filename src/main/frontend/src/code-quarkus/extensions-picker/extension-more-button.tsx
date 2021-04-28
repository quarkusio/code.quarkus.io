import React, { useState } from 'react';
import { CopyToClipboard, useAnalytics } from '../../core';
import { Dropdown, DropdownItem, DropdownPosition, KebabToggle } from '@patternfly/react-core';
import { MapIcon } from '@patternfly/react-icons';
import { ExtensionEntry } from './extensions-picker';

export function ExtensionMoreButton(props: ExtensionEntry) {
    const [isMoreOpen, setIsMoreOpen] = useState(false);
    const analytics = useAnalytics();
    const gav = `${props.id}:${props.version}`;
    const gavArray = gav.split(':');
    const addMvnExt = `./mvnw quarkus:add-extension -Dextensions="${props.id}"`;
    const addGradleExt = `./gradlew addExtension --extensions="${props.id}"`;
    const xml = `<dependency>\n            <groupId>${gavArray[0]}</groupId>\n            <artifactId>${gavArray[1]}</artifactId>\n        </dependency>`;
    const closeMore = () => {
        setTimeout(() => setIsMoreOpen(false), 1000);
    };

    const openGuide = () => {
        analytics.event('Extension', 'Click "Open Extension Guide" link', props.id);
        closeMore();
    };

    const moreItems = [
        (
            <DropdownItem key="maven">
                <CopyToClipboard event={['Extension', 'Copy the command to add it with Maven', props.id]} content={addMvnExt}
                                 tooltipPosition="left" onClick={closeMore} zIndex={201}
                >Copy the command to add it with Maven</CopyToClipboard>
            </DropdownItem>
        ),
        (
            <DropdownItem key="gradle">
                <CopyToClipboard event={['Extension', 'Copy the command to add it with Gradle', props.id]}
                                 content={addGradleExt} tooltipPosition="left" onClick={closeMore} zIndex={201}
                >Copy the command to add it with Gradle</CopyToClipboard>
            </DropdownItem>
        ),
        (
            <DropdownItem key="xml">
                <CopyToClipboard event={['Extension', 'Copy the extension pom.xml snippet', props.id]} content={xml}
                                 tooltipPosition="left" onClick={closeMore} zIndex={201}
                >Copy the extension pom.xml snippet</CopyToClipboard>
            </DropdownItem>
        ),
        (
            <DropdownItem key="id">
                <CopyToClipboard event={['Extension', 'Copy the GAV', props.id]} content={gav} tooltipPosition="left"
                                 onClick={closeMore} zIndex={201}>Copy the extension GAV</CopyToClipboard>
            </DropdownItem>
        )
    ];

    if (props.guide) {
        moreItems.push(
            <DropdownItem key="guide" href={props.guide} target="_blank" onClick={openGuide}>
                <MapIcon/> Open Extension Guide
            </DropdownItem>
        );
    }

    return (
        <Dropdown
            isOpen={isMoreOpen}
            position={DropdownPosition.left}
            toggle={<KebabToggle onToggle={() => setIsMoreOpen(!isMoreOpen)}/>}
            onClick={(e) => e.stopPropagation()}
            dropdownItems={moreItems}
        />
    );
}
