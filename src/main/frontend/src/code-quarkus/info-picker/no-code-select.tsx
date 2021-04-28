import { InputProps, useAnalyticsEditionField } from '../../core';
import React, { ChangeEvent } from 'react';
import { Tooltip } from '@patternfly/react-core';


export const NoCodeSelect = (props: InputProps<boolean>) => {
    const onChangeWithDirty = useAnalyticsEditionField('no-code', props.onChange)[1];
    const adaptedOnChange = (e: ChangeEvent<HTMLSelectElement>) => {
        onChangeWithDirty(e.target.value === 'true', e);
    };
    return (
        <Tooltip
            position="right"
            content={<span>The flag <span className="codestart-icon" /> means the extension provides starter code. You can also choose to have an empty Quarkus project.</span>}
            exitDelay={0}
            zIndex={200}
        >
            <div className="form-group">
                <label className="form-group-label" htmlFor="no-code" aria-label="Starter Code"><span><span className="codestart-icon" />Starter Code</span></label>

                <select id="no-code" value={props.value ? 'true' : 'false'} onChange={adaptedOnChange} className={'form-group-control'}>
                    <option value={'false'}>Yes</option>
                    <option value={'true'}>No</option>
                </select>
            </div>
        </Tooltip>
    );
};
