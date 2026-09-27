# PvPPokeGo V05 — Copilot Hardening

## Scope

V05 keeps PvPPokeGo as an external, read-only visual overlay for Pokémon GO. It does not inject code, hook game memory, send taps, use Accessibility to battle automatically, or modify Pokémon GO.

## Improvements integrated

1. Battle-presence confidence fusion
   - Combines native PvP HP bars, top information cards, OCR battle text and switch prompts.
   - Uses hysteresis so the HUD neither appears on the map from one false OCR hit nor vanishes during Charged/switch animations.

2. Enemy energy as a range when uncertain
   - Energy remains per enemy Pokémon through switches.
   - Exact observed Charged Move subtracts exact cost.
   - If OCR confirms a Charged Move but cannot identify which move, V05 preserves a plausible min/max residual-energy range instead of guessing the cheapest Charged Move.
   - UI can label energy as CONFIRMED, ESTIMATED or RANGE.

3. Own-energy model
   - Player Fast Moves are inferred independently from drops in the opponent HP bar.
   - Own likely moveset and energy are remembered per Pokémon through switches.
   - OCR of the player's Charged usage subtracts the observed Charged cost.
   - The copilot can recommend USE <move>, +1 FAST or SEGURE ENERGIA.

4. Dynamic top-HUD anchor
   - Detects the top edge of Pokémon GO's native white player/opponent information cards.
   - Default overlay type/matchup chips are placed in the free strip above the cards and below Android system UI.
   - Explicit manual edit positions still override the automatic anchor.

5. Unified battle visuals
   - Enemy move circles use the same visual language as Pokémon GO Charged buttons: dark uncharged area, type-colored energy fill, circular border, centered type glyph and optional charge dots.

6. Independent league badge
   - Separate draggable triangular badge for Great, Ultra, Master or Auto.
   - It does not move with the settings gear.

7. Advanced HUD editor
   - Free drag.
   - Independent scale, opacity, lock and hide per major HUD block.
   - One-step position undo while editing.
   - Save/exit and full customization reset.

8. Modern Android target
   - compileSdk 36 / targetSdk 36.
   - Android Gradle Plugin 8.10.1 and Gradle 8.11.1 in CI.

9. Smarter reserves/switch choice
   - Reserve score uses both typing and likely movesets against the active opponent.
   - Labels available as MELHOR, NEUTRO or EVITAR.

10. Smarter shield advice
    - Compares plausible cheap bait versus higher-damage Charged options.
    - Can surface PODE SER BAIT, ESCUDO, NÃO ESCUDE or ESCUDO OPCIONAL.

11. Enemy history
    - Optional compact history of recently seen enemy Pokémon, Fast count, energy/range and last observed Charged Move.

12. Basic and advanced quick menus
    - Basic menu keeps only battle-time essentials.
    - Advanced menu contains OCR/display/counters/history/debug and reset controls.
    - Gear remains icon-only; internal rows can show icon + text or collapse to icons-only.

## Live-test priorities

Because frame/OCR thresholds vary across device resolution, aspect ratio, status-bar insets, weather/background and Pokémon animation, validate these first on the target phone:

- HUD stays hidden outside real PvP.
- Top chips remain fully visible and do not cover native type icons or white info cards.
- Enemy energy rises only when the opponent's Fast Move damages the player.
- Own energy rises only when the player's Fast Move damages the opponent.
- Charged usage subtracts energy exactly when move name is recognized; unidentified Charged creates a range rather than a false exact value.
- Switching away and back restores both sides' stored energy.
- Reserve recommendations line up with the actual two switch choices.
- Editor drag/scale/opacity/lock/hide/undo/save/reset work without affecting Pokémon GO input outside edit mode.

## Development rule

Reliability has priority over adding new controls. When visual evidence is ambiguous, display uncertainty instead of presenting a guessed exact value as fact.
