import * as React from 'react';
import { cleanup, fireEvent, render, act } from '@testing-library/react';
import { QuarkusApp } from '../quarkus-app';


afterEach(() => {
  console.log('cleanup()');
  cleanup();
});

it('Render QuarkusApp', async () => {
  render(<QuarkusApp />);
});