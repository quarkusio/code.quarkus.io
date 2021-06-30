import { InputProps, useAnalyticsEditionField } from '../../core';
import React, { ChangeEvent } from 'react';
import { FormGroup, Tooltip } from '@patternfly/react-core';


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
            <FormGroup
                fieldId="no-code"
                label={<span><span className="codestart-icon" />Starter Code</span>}
                aria-label="Starter Code">
                <select id="no-code" value={props.value ? 'true' : 'false'} onChange={adaptedOnChange} className={'pf-c-form-control'}>
                    <option value={'false'}>Yes</option>
                    <option value={'true'}>No</option>
                </select>
            </FormGroup>
        </Tooltip>
    );
};
