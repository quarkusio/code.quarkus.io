import React, {useState} from 'react';
import {ExtensionRow} from "./extension-row";
import styled from 'styled-components';
import {FaExclamation, FaTrashAlt, FaAngleDown, FaAngleUp, FaLightbulb} from 'react-icons/fa';
import {Alert} from 'react-bootstrap';
import {ExtensionEntry, TagEntry} from "./extensions-picker";
import classNames from 'classnames';
import {Platform} from "../api/model";


const SelectedExtensionsDiv = styled.div`


    button.btn.btn-clear {
        margin-left: 10px;
        color: var(--clearSelectedExtensionButtonTextColor);
        border: 1px solid var(--clearSelectedExtensionButtonBorderColor);

        svg {
            color: var(--clearSelectedExtensionButtonBorderColor);
        }

        &:hover {
            background-color: rgba(0, 0, 0, 0.2);
            background-blend-mode: multiply;
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

    .alert-info {
        background-color: var(--selectedExtensionAlertInfoBg);
        color: var(--selectedExtensionAlertInfoTextColor);
        padding: 5px;
        font-size: 0.9rem;
    }

    &.picker {
        background-color: var(--extensionsPickerListBg);
        border: none;
        margin-top: 20px;

        h4 {
            padding: 10px;
            margin: 0;
            background-color: var(--mainContainerControlBarBg);
            color: var(--mainContainerControlBarTextColor);
            display: flex;
            font-weight: normal;

            span {
                flex-grow: 1;
            }

            .count {
                font-weight: bold;
            }
        }

        h5 {
            padding: 10px;
            margin: 0;
        }
    }

    &.cart {
        display: flex;
        flex-direction: column;

        .alert-warning {
            background-color: rgba(255, 255, 255, 0.1);
            background-blend-mode: multiply;
            color: var(--dropdownMenuTextColor);
            border: none;
            padding: 3px;
        }

        .extension-row {
            flex-wrap: nowrap;
            height: 30px;
            line-height: 30px;
            border: none;
            padding: 0 5px;

            .extension-name {
                line-height: 30px;
                height: 30px;

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
    confirm("This will remove all selected extensions. Do you want to continue??") && props.remove('*', 'Selection clear');
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
      {props.extensions.length === 0 && (
        <Alert variant="warning">
          <FaExclamation/>&nbsp;You haven't selected any extension for your Quarkus application. Browse and select from
          the list below.
        </Alert>
      )}
      {props.extensions.length > 0 && (
        <>
          <h4>
            <span><span className="count">{props.extensions.length}</span> selected {props.extensions.length > 1 ? 'extensions' : 'extension'}</span>
            {props.extensions.length > 0 &&
                <button className="btn btn-light btn-clear" onClick={clear} aria-label="Clear extension selection">
                    <FaTrashAlt/> Clear selection
                </button>}
          </h4>
          {layout === 'picker' && (
            <Alert variant="info">
              <FaLightbulb/>&nbsp;Find more extensions with search, filters, or the full list. Clear selection to show the presets again.
            </Alert>
          )}
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
          <h5 onClick={flipTransitive}>{transitiveExtensions.length} transitive {transitiveExtensions.length > 1 ? 'extensions' :  'extension'} {showTransitive ?
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

