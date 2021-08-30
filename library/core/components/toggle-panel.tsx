import React, { Fragment, ReactNode } from 'react';
import createPersistedState from 'use-persisted-state';
import './toggle-panel.scss';
import { Button } from 'react-bootstrap';
import { useAnalytics } from '@quarkusio/code-quarkus.core.analytics';
import { FaCaretSquareDown, FaCaretSquareLeft, FaCaretSquareRight, FaCaretSquareUp } from 'react-icons/fa';

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
  const [ open, setOpen ] = useTogglePanelState(false);
  const mode = props.mode || 'vertical';
  const CloseIcon = mode === 'horizontal' ? <FaCaretSquareLeft/> : <FaCaretSquareUp/>;
  const OpenIcon = mode === 'horizontal' ? <FaCaretSquareRight/> : <FaCaretSquareDown/>;
  const flip = () => {
    if (props.event && props.event.length === 2) {
      analytics.event(props.event[0], props.event[1], open ? 'Hide' : 'Show')
    }
    setOpen(!open);
  };
  return (
    <Fragment>
      <div className={`toggle-panel ${mode} ${(open ? 'open' : '')}`}>
        {props.children}
      </div>
      <Button
        as="a"
        className="toggle-button"
        aria-label="Toggle panel"
        onClick={flip}
      >
        {open ? (<React.Fragment>{CloseIcon}<span className="toggle-label">{props.closeLabel || 'Close'}</span></React.Fragment>) : (
          <React.Fragment>{OpenIcon}<span className="toggle-label">{props.openLabel || 'Open'}</span></React.Fragment>)}
      </Button>
    </Fragment>
  );
}
