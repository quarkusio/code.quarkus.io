import React, { useState } from 'react';
import classNames from 'classnames';
import { CheckSquareIcon, EllipsisVIcon, OutlinedSquareIcon, TrashAltIcon } from '@patternfly/react-icons';
import { ExtensionEntry } from './extensions-picker';
import { ExtensionTags } from './extension-tags';
import { ExtensionMoreButton } from './extension-more-button';


export interface ExtensionRowProps extends ExtensionEntry {
    selected: boolean;
    keyboardActived: boolean;
    detailed?: boolean;
    default: boolean;
    buildTool: string;

    onClick(id: string): void;
}

export function ExtensionRow(props: ExtensionRowProps) {
    const [hover, setHover] = useState(false);

    const onClick = () => {
        if (props.default) {
            return;
        }
        props.onClick(props.id);
        setHover(false);
    };

    const activationEvents = {
        onClick,
        onMouseEnter: () => setHover(true),
        onMouseLeave: () => setHover(false),
    };

    const description = props.description || '...';
    const selected = props.selected || props.default;

    return (
        <div {...activationEvents} className={classNames('extension-item', {
            'keyboard-actived': props.keyboardActived,
            hover,
            selected,
            'by-default': props.default
        })}>
            {props.detailed && (
                <div
                    className="extension-selector"
                    aria-label={`Switch ${props.id} extension`}
                >
                    {!selected && !(hover) && <OutlinedSquareIcon/>}
                    {(hover || selected) && <CheckSquareIcon/>}
                </div>
            )}

            <div className="extension-summary">
                <span className="extension-name" title={`${props.name} (${props.version})`}>{props.name}</span>
                {props.tags && props.tags.map((s, i) => <ExtensionTags key={i} status={s}/>)}
            </div>

            {!props.detailed && (
                <div
                    className="extension-remove"
                >
                    {hover && props.selected && <TrashAltIcon/>}
                </div>
            )}

            {props.detailed && (
                <div className="extension-details">
                    <div
                        className="extension-description" title={description}
                    >{description}</div>
                    <div className="extension-more">
                        {!hover && (
                            <button aria-label="Actions" className="pf-c-dropdown__toggle" type="button" aria-expanded="false">
                                <EllipsisVIcon/>
                            </button>
                        )}
                        {hover && <ExtensionMoreButton {...props} />}
                    </div>
                </div>
            )}
        </div>
    );
}
