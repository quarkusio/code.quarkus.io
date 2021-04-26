import { InputProps, useAnalyticsEditionField } from '../../core';
import React, { ChangeEvent } from 'react';
import { FormGroup } from '@patternfly/react-core';

export const BuildToolSelect = (props: InputProps<string>) => {
    const onChangeWithDirty = useAnalyticsEditionField('buildTool', props.onChange)[1];
    const adaptedOnChange = (e: ChangeEvent<HTMLSelectElement>) => {
        onChangeWithDirty(e.target.value, e);
    };
    return (
        <FormGroup
            fieldId="buildTool"
            label="Build Tool"
            aria-label="Choose build tool">
            <select id="buildtool" value={props.value} onChange={adaptedOnChange} className={'pf-c-form-control'}>
                <option value={'MAVEN'}>Maven</option>
                <option value={'GRADLE'}>Gradle (Preview)</option>
                <option value={'GRADLE_KOTLIN_DSL'}>Gradle with Kotlin DSL (Preview)</option>
            </select>
        </FormGroup>
    );
};