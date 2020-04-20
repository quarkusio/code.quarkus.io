import React, { useState } from 'react';
import { useAnalytics } from '../../core/analytics';
import { Dropdown, DropdownItem, DropdownPosition, KebabToggle } from '@patternfly/react-core';
import { GithubIcon } from '@patternfly/react-icons';
import { createOnGitHub } from '../api/quarkus-project-utils';
import { QuarkusProject } from '../api/model';

export function GenerateMoreButton(props: { project: QuarkusProject, githubClientId?: string }) {
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
    setIsMoreOpen(true);
    setIsMoreClosing(false);
  };

  const moreItems = [];

  if (props.githubClientId) {
    const githubClick = () => {
      analytics.event('Extension', 'Click "Create on Github"');
      createOnGitHub(props.project, props.githubClientId!);
    };

    moreItems.push(
      <DropdownItem key="guide" variant="icon" onClick={githubClick}>
        <GithubIcon/> Generate and push to GitHub
      </DropdownItem>
    );
  }

  return moreItems.length > 0 ? (
        <Dropdown
          isOpen={isMoreOpen}
          position={DropdownPosition.left}
          onMouseLeave={closeMore}
          onMouseEnter={openMore}
          toggle={<KebabToggle className="generate-more" isOpen={isMoreOpen}/>}
          onClick={(e) => e.stopPropagation()}
          dropdownItems={moreItems}
        />
      ) : (<React.Fragment />);
}
