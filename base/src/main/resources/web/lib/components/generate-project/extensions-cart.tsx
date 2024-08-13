import React, {useEffect, useState} from 'react';
import './extensions-cart.scss';
import {FaRocket} from 'react-icons/fa';
import {Button, ButtonGroup, Dropdown} from 'react-bootstrap';
import DropdownToggle from 'react-bootstrap/DropdownToggle';
import {ExtensionEntry, TagEntry} from '../extensions-picker/extensions-picker';
import {useAnalytics} from '../../core/analytics';
import {InputProps} from '../../core/types';
import _ from 'lodash';
import classNames from 'classnames';
import {SelectedExtensions} from "../extensions-picker/selected-extensions";

export interface ExtensionsCartValue {
  extensions: ExtensionEntry[];
}

export interface ExtensionsCartProps extends InputProps<ExtensionsCartValue> {
  tagsDef: TagEntry[];
}


export function ExtensionsCart(props: ExtensionsCartProps) {
  const [isOpen, setIsOpen] = useState(false);
  const [openedFromChange, setOpenedFromChange] = useState(false);

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

  function onRemove(id: string, type: string) {
    props.onChange({extensions: _.filter(props.value.extensions, e => e.id !== id && id !== '*')});
    analytics.event('Unselect extension', {extension: id, type, element: 'extension-cart'});
  }

  return (
    <Dropdown className={classNames('extensions-cart', openedFromChange && 'opened-from-change')} as={ButtonGroup}
              show={isOpen || openedFromChange} onMouseLeave={onMouseLeaveFn}>
      <DropdownToggle as={Button} aria-label="Selected extensions" className="extensions-cart-button"
                      onMouseEnter={onMouseEnterFn}>
        <FaRocket/>
        {props.value.extensions.length}
      </DropdownToggle>

      <Dropdown.Menu onMouseEnter={onMouseEnterFn} align="left">
        <SelectedExtensions extensions={props.value.extensions} remove={onRemove} tagsDef={props.tagsDef}/>
      </Dropdown.Menu>
    </Dropdown>
  );
}
