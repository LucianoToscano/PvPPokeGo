# Smart Battle Automation Core

This module adds decision logic for PvPPokeGo's battle-assist/automation layer.
It is independent of Android screen coordinates, OCR and AccessibilityService.

## Charged move modes

- `FIRST_READY`: classic behavior; use the first charged move that is ready.
- `TYPE_PRIORITY`: prefer the ready move with the best type effectiveness.
- `SMART`: score both charged buttons using type effectiveness, STAB, effective power, damage per energy, energy cost, shields and low-HP finisher pressure.

The decision includes `buttonIndex`, so the UI/executor can intentionally choose charged button 1 or 2. This directly avoids the common "always use the first charged attack" limitation.

## Shield modes

- `OFF`: never shield automatically.
- `NORMAL`: shield strong super-effective threats.
- `SMART`: combine type effectiveness, estimated threat and current HP.
- `SAFE`: more conservative shielding.

For best results, the caller should pass only enemy charged moves that are plausible from `BattleEnergyTracker` at the current tracked energy.

## Feature toggles

`BattleAutomationSettings` exposes independent switches for:

- master enable;
- automatic fast attack;
- automatic charged attack;
- charged selection mode;
- automatic shield;
- shield mode.

## Integration contract

`BattleAutomationController` emits action intents only:

- `TapFastAttack`
- `TapChargedAttack(buttonIndex, moveId, ...)`
- `TapShield(...)`

The Android app layer may use these intents either to highlight a recommendation in assist-only mode or to dispatch a user-enabled gesture through an Android accessibility adapter.

The pure core intentionally does not contain screen coordinates or gesture-dispatch code. This keeps battle logic testable and prevents UI code from duplicating or overriding the decision engine.
