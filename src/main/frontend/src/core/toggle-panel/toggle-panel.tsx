import { Button } from '@patternfly/react-core';
import { CaretSquareDownIcon, CaretSquareUpIcon, CaretSquareRightIcon, CaretSquareLeftIcon } from '@patternfly/react-icons';
import React, { Fragment, ReactNode } from 'react';
import createPersistedState from 'use-persisted-state';
import './toggle-panel.scss';
import { useAnalytics } from '../analytics/analytics-context';

interface TogglePanelProps {
  id: string;
  closeLabel?: string;
  openLabel?: string
  event?: string[];
  mode?: 'vertical' | 'horizontal';
  children: ReactNode;
}

export function TogglePanel(props: TogglePanelProps) {
  const analytics = useAnalytics();
  const useTogglePanelState = createPersistedState(props.id);
  const [open, setOpen] = useTogglePanelState(false);
  const mode = props.mode || 'vertical';
  const CloseIcon = mode === 'horizontal' ? <CaretSquareLeftIcon /> : <CaretSquareUpIcon />;
  const OpenIcon = mode === 'horizontal' ? <CaretSquareRightIcon /> : <CaretSquareDownIcon />;
  const flip = () => {
    if(props.event && props.event.length === 2) {
      analytics.event(props.event[0], props.event[1], open ? "Hide" : "Show")
    }
    setOpen(!open);
  };
  return (
    <Fragment>
      <div className={`toggle-panel ${mode} ${(open ? 'open' : '')}`}>
        {props.children}
      </div>
      <div className="toggle-button">
        <Button
          // @ts-ignore
          component="a"
          variant="link"
          aria-label="Toggle panel"
          onClick={flip}
        >
          {open ? (<span>{CloseIcon} {props.closeLabel || 'Close'}</span>) : (<span>{OpenIcon} {props.openLabel || 'Open'}</span>)}
        </Button>
      </div>
    </Fragment>
  );
}
