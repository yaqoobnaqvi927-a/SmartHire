# SmartHire Frontend Rules & Operational Mandates

- **Framework**: React 18+ with TypeScript using Vite.
- **Component Pattern**: Functional components and React Hooks are mandatory.
- **Styling Paradigm**: Tailwind CSS strictly. No inline styles or custom CSS files.
  - *Color Palette Strategy*: Minimal light theme (slate grays: `text-slate-800`, `text-slate-500`).
  - *Theme Accents*: Green accent system for actions, successes, and AI scores (`bg-emerald-500`, `text-emerald-600`, `border-emerald-200`).
- **Code Quality**: Strict ESLint rules enforced. Fully typed with explicit TypeScript interfaces.
- **Data Fetching**: Extracted into dedicated service files. Must use `Axios`.
- **Architecture**: Semantic HTML5 layout hierarchy.
