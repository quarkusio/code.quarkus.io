import React, {useState} from 'react';
import {ExtensionRow} from "./extension-row";
import styled from 'styled-components';
import {FaExclamation, FaTrashAlt, FaAngleDown, FaAngleUp} from 'react-icons/fa';
import {Alert} from 'react-bootstrap';
import {ExtensionEntry, TagEntry} from "./extensions-picker";
import classNames from 'classnames';
import {Platform} from "../api/model";


const SelectedExtensionsDiv = styled.div`
    button.btn.btn-clear {
        color: var(--warningColor);
        font-weight: bold;
        float: right;

        svg {
            color: var(--warningColor);
        }


    }

    h5 {
        margin-top: 20px;
        cursor: pointer;
        user-select: none;
        
        svg {
            width: 20px;
            vertical-align: middle;
        }
    }

    &.picker {
        h4 {
            border-bottom: 1px solid var(--extensionsPickerCategoryUnderlineColor);
        }
    }

    &.cart {
        display: flex;
        flex-direction: column;

        .alert-warning {
            background-color: transparent;
            color: var(--dropdownMenuTextColor);
            border: none;
            padding: 0;
        }

        .extension-row {
            flex-wrap: nowrap;
            height: 22px;
            line-height: 22px;

            .extension-name {
                line-height: 22px;
                height: 22px;
            }

            .extension-origin-platform {
                margin-left: 5px;
            }

            .extension-id {
                display: none;
            }

            .extension-tag {
                margin: 0 0 0 5px;
            }
        }

        .extension-summary:before {
            content: '-';
            margin-right: 5px;
            margin-left: 5px;
        }

        .extension-summary {
            flex-grow: 1;
            text-overflow: ellipsis;
        }

        .extension-remove {
            order: 3;
        }
    }

`


export const SelectedExtensions = (props: {
  layout?: 'cart' | 'picker',
  extensions: ExtensionEntry[],
  tagsDef: TagEntry[],
  remove: (id: string, type: string) => void,
  platform: Platform
}) => {
  const [showTransitive, setShowTransitive] = useState<boolean>(false);
  const clear = () => {
    props.remove('*', 'Selection clear');
  };

  function flipTransitive() {
    setShowTransitive(!showTransitive);
  }

  const layout = props.layout || 'cart';
  let transitiveExtensions = [...new Set<string>(props.extensions
    .flatMap((ex) => ex.transitiveExtensions)
    .filter(id => props.platform.extensionById[id]))]
    .map(id => props.platform.extensionById[id])
    .filter(ex => props.extensions.indexOf(ex) < 0);
  return (
    <SelectedExtensionsDiv className={classNames('selected-extensions', layout)}>
      <h4>
        Selected Extensions
        {props.extensions.length > 0 &&
            <button className="btn btn-light btn-clear" onClick={clear} aria-label="Clear extension selection">
                <FaTrashAlt/>Clear selection
            </button>}
      </h4>
      {props.extensions.length === 0 && (
        <Alert variant="warning">
          <FaExclamation/>&nbsp;You haven't selected any extension for your Quarkus application. Browse and select from
          the list below.
        </Alert>
      )}
      {props.extensions.length > 0 && (
        <>
          <div className="extension-list-wrapper">
            {
              props.extensions.map((ex, i) => (
                <ExtensionRow
                  {...ex}
                  key={i}
                  selected={true}
                  onClick={() => props.remove(ex.id, 'Selection')}
                  layout={layout}
                  tagsDef={props.tagsDef}
                />
              ))
            }

          </div>
          <h5 onClick={flipTransitive}>Transitive extensions ({transitiveExtensions.length}) {showTransitive ?
            <FaAngleUp/> : <FaAngleDown/>}</h5>
          {showTransitive &&
              <div className="extension-list-wrapper transitive">
                {
                  transitiveExtensions.map((ex, i) => (
                    <ExtensionRow
                      {...ex}
                      key={i}
                      selected={true}
                      transitive={true}
                      layout={layout}
                      tagsDef={props.tagsDef}
                    />
                  ))
                }
              </div>
          }
        </>
      )}
    </SelectedExtensionsDiv>
  );
}

