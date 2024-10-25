import styled from 'styled-components';
import {TagEntry} from "./extensions-picker";
import {Platform, Preset} from "../api/model";

import {useAnalytics} from "../../core/analytics";

interface PresetsProps {
  platform: Platform,
  select?: (id: string, type: string) => void
}


const PresetsPanelDiv = styled.div`


    .panel-title {
        font-weight: bold;
        color: var(--extensionsPickerCategoryTextColor);
        font-size: 1.3rem;
        margin: 10px 0 10px 0;
        height: 30px;
    }

    .presets-list {
        display: flex;
        flex-wrap: wrap;
        justify-items: left;
        gap: 10px;
    }

    .preset-card {
        flex-basis: 292px;
        background-color: var(--presetsCardBackgroundColor);
        border: 1px solid var(--presetsCardBorderColor);
        display: flex;
        flex-direction: column;
        align-items: center;
        padding: 20px;
        cursor: pointer;
    }

    .preset-title {
        color: var(--presetsCardTextColor);
        font-size: 1.1rem;
        text-align: center;
    }

    .preset-icon {
        flex-basis: 110px;
    }

    .preset-icon img {
        width: 90px;
    }
    
`;

export const PresetCard = (props: { preset: Preset, tagsDef: TagEntry[], onClick?: () => void }) => {
  return (
    <div className="preset-card" onClick={props.onClick} aria-label={`Select ${props.preset.key} preset`}>
      <div className="preset-icon"><img src={props.preset.icon}/></div>
      <div className="preset-title">{props.preset.title}</div>
    </div>
  );
}

export const PresetsPanel = (props: PresetsProps) => {
  let analytics = useAnalytics();
  const context = {element: 'preset-picker'};
  const extensionById = props.platform.extensionById;
  const presets = props.platform.presets.map(p => ({
    ...p, resolvedExtensions: p.extensions.filter(e => extensionById[e]).map(e => extensionById[e])
  } as Preset))

  const selectPreset = (preset: Preset) => {
    analytics.event('Select preset', {preset: preset.key, ...context});
    preset.extensions.forEach(e => props.select(e, "presets"));
  };
  return (
    <PresetsPanelDiv className="presets-panel">
      <div className="panel-title">Start from an extensions preset</div>
      <div className="presets-list">
        {presets.map(p => (
          <PresetCard key={p.key} preset={p} tagsDef={props.platform.tagsDef} onClick={() => selectPreset(p)}/>
        ))}
      </div>
    </PresetsPanelDiv>
  );
}