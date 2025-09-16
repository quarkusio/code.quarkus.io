import styled from 'styled-components';
import {TagEntry} from "./extensions-picker";
import {Platform, Preset} from "../api/model";

import {useAnalytics} from "../../core/analytics";

interface PresetsProps {
  platform: Platform,
  select?: (id: string, type: string) => void
}


const PresetsPanelDiv = styled.div`
    background-color: var(--presetsPanelBg);

    .panel-title {
        font-size: 1.1rem;
        margin: 10px 0 0 0;
        height: 40px;
        display: flex;
        align-items: center;
        padding: 10px;
        background-color: var(--presetsPanelBg);
        color: var(--presetsTitleTextColor);
        
        .extension-icon {
            margin-right: 3px;
        }
    }

    .presets-list {
        display: flex;
        flex-wrap: wrap;
        justify-items: left;
    }

    .preset-card {
        flex-basis: 300px;
        background-color: var(--presetsCardBackgroundColor);
        border: 1px solid var(--presetsCardBorderColor);
        display: flex;
        flex-direction: column;
        align-items: center;
        padding: 20px;
        cursor: pointer;
    }
    
    .preset-card:hover {
        background-color: var(--presetsCardHoverBackgroundColor);
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

export const PresetCard = (props: { preset: Preset, onClick?: () => void }) => {
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
      <div className="panel-title main-title"><span className="extension-icon"></span>&nbsp;Start with a preset of extensions</div>
      <div className="presets-list">
        {presets.map(p => (
          <PresetCard key={p.key} preset={p} onClick={() => selectPreset(p)}/>
        ))}
      </div>
    </PresetsPanelDiv>
  );
}