import { Button } from '@patternfly/react-core';
import { CaretSquareDownIcon, CaretSquareUpIcon, CaretSquareRightIcon, CaretSquareLeftIcon } from '@patternfly/react-icons';
import React, { Fragment, ReactNode } from 'react';
import { useSessionStorageWithObject } from 'react-use-sessionstorage';
import './toggle-panel.scss';


interface TogglePanelProps {
  id: string;
  closeLabel?: string;
  openLabel?: string
  mode?: 'vertical' | 'horizontal';
  children: ReactNode;
}

export function TogglePanel(props: TogglePanelProps) {
  const [open, setOpen] = useSessionStorageWithObject(props.id, false);
  const mode = props.mode || 'vertical';
  const CloseIcon = mode === 'horizontal' ? <CaretSquareLeftIcon /> : <CaretSquareUpIcon />;
  const OpenIcon = mode === 'horizontal' ? <CaretSquareRightIcon /> : <CaretSquareDownIcon />;
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
          onClick={() => setOpen(!open)}
        >
          {open ? (<span>{CloseIcon} {props.closeLabel || 'Close'}</span>) : (<span>{OpenIcon} {props.openLabel || 'Open'}</span>)}
        </Button>
      </div>
    </Fragment>
  );
}
