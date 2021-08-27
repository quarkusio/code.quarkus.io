import './extensions-cart.scss';
import { FaRocket } from 'react-icons/fa';
import { Alert, Button, ButtonGroup, Dropdown } from 'react-bootstrap';
import React, { useEffect, useState } from 'react';
import { ExtensionRow } from '../extensions-picker/extension-row';
import DropdownToggle from 'react-bootstrap/DropdownToggle';
import { ExtensionEntry } from '../extensions-picker/extensions-picker';
import { InputProps, useAnalytics } from '../../core';
import _ from 'lodash';
import classNames from 'classnames';

const SelectedExtensions = (props: any) => {
  return (
    <div className="selected-extensions">
      <h4>Selected Extensions</h4>
      {props.extensions.length === 0 && (
        <Alert variant="warning" >
          You haven't selected any extension for your Quarkus application. Browse and select from the list below.
        </Alert>
      )}
      {props.extensions.length > 0 && (
        <div className="extension-list-wrapper">
          {
            props.extensions.map((ex, i) => (
              <ExtensionRow
                {...ex}
                key={i}
                selected={true}
                onClick={() => props.remove(ex.id, 'Selection')}
                pickerLayout={false}
              />
            ))
          }
        </div>
      )}
    </div>
  );
}


export interface ExtensionsCartValue {
  extensions: ExtensionEntry[];
}

export interface ExtensionsCartProps extends InputProps<ExtensionsCartValue> {
}


export function ExtensionsCart(props: ExtensionsCartProps) {
  const [ isOpen, setIsOpen ] = useState(false);
  const [ openedFromChange, setOpenedFromChange ] = useState(false);

  const analytics = useAnalytics();

  function onMouseEnterFn(e) {
    onToggleFn(true);
  }

  function onMouseLeaveFn(e) {
    onToggleFn(false);
  }

  function onToggleFn(open: boolean) {
    setIsOpen(open);
    setOpenedFromChange(false);
  }

  function onRemove(id: string, origin: string) {
    props.onChange({ extensions: _.filter(props.value.extensions, e => e.id !== id && id !== '*') });
    analytics.event('UX', 'Extension - Unselect', origin);
  }

  useEffect(() => {
    let timeout: any;
    if (props.value.extensions.length > 0) {
      setOpenedFromChange(true);
      timeout = setTimeout(() => setOpenedFromChange(false), 3000);
    }
    return () => {
      if(timeout) {
        clearTimeout(timeout);
      }
    };
  }, [ props.value.extensions.length ])

  return (
    <Dropdown className={classNames('extensions-cart', openedFromChange && 'opened-from-change')} as={ButtonGroup} show={isOpen || openedFromChange} onMouseLeave={onMouseLeaveFn}>
      <DropdownToggle as={Button} aria-label="Selected extensions" className="extensions-cart-button"
        onMouseEnter={onMouseEnterFn}>
        <FaRocket/>
        {props.value.extensions.length}
      </DropdownToggle>

      <Dropdown.Menu onMouseEnter={onMouseEnterFn} align="left">
        <SelectedExtensions extensions={props.value.extensions} remove={onRemove}/>
      </Dropdown.Menu>
    </Dropdown>
  );
}
