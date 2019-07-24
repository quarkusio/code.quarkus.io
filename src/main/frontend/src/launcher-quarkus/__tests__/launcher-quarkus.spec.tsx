import * as React from 'react';
import { cleanup, fireEvent, render, act } from '@testing-library/react';
import { LauncherQuarkus } from '../launcher-quarkus';


afterEach(() => {
  cleanup();
});

it('Render LauncherQuarkus', async () => {
  render(<LauncherQuarkus config={{ environment: 'test' }} />);
});