# PvPPokeGo — ícones de tipos, HUD, matchup e ranking

**Data:** 13/10/2026. **Código de referência:** legacy 0.5.37. **Ação:** corrigida omissão da primeira biblioteca de PNGs. **Não modificado o APK nem o código de produção.**

## Onde estão os ícones no código

| Grupo | Origem real | Entrega recuperada |
|---|---|---|
| 18 tipos Pokémon | `overlay/TypeGlyphPaths.kt`: 18 arrays vetoriais com contornos fill EVEN_ODD | 18 SVG + 18 PNG silhuetas, 18 coloridos, 18 cinza (54 pares PNG/SVG) |
| Cadeado aberto/fechado | `overlay/BattleOverlayView.kt`: método `drawLayoutLock` com `Canvas.drawRoundRect` / `drawArc` | `cadeado_aberto.png/svg`, `cadeado_fechado.png/svg` |
| Olho com risco/sem risco | `overlay/BattleOverlayView.kt`: `drawVisibilityToggle` com `drawOval`/círculos/linha | `olho_com_risco.png/svg`, `olho_sem_risco.png/svg`; **nomeado por aparência** devido à lógica do `hidden` no draw |
| Forte, neutro, fraco, desconhecido | `drawMatchupIndicator`, cores #14be50, #919191, #e12d37, #696969; símbolos ✓/●/×/? | quatro SVG + quatro PNG com cores e símbolos próximos do desenho Canvas |
| Rank #5/#18/#12 | Em imagens de REFERÊNCIA `1000813552.png`; **não são texturas isoladas no APK** | quatro recortes do mockup, explicitamente marcados REFERENCIA/NÃO EMBUTIDO |
| Ranking local em tempo real | `ui/ArenaDashboard.kt`: texto/emoji e cards; `ui/TeamBuilderPanel.kt`: IV Rank texto/calculado | **não existe PNG original de rank para extrair** |
| Outros botões, ícones Mega/Sombroso/escudo | Variáveis do HUD/Compose, glifos ou partes de imagens de referência | **não afirmar que são PNGs originais separados**; requer inventário específico |

## Pasta organizada dentro da nova biblioteca

`09_Icones_HUD_Tipos_Vetorizados_e_Ranks_Referencia/`

Dentro dela:
- `01_Tipos_Pokemon_18_Tipos_Originais/Icones_Coloridos/`
- `01_Tipos_Pokemon_18_Tipos_Originais/Silhuetas_Brancas/`
- `01_Tipos_Pokemon_18_Tipos_Originais/Estados_Desligados_Cinzas/`
- `02_Cadeado_e_Olho_HUD_Fieis_ao_Codigo/`
- `03_Indicadores_Forte_Neutro_Fraco/`
- `04_Ranks_Referencias_Visuais_Nao_Inclusos_Como_PNG_no_APK/`
- `05_Codigo_Fonte_Historico_e_Explicacao/`
- `06_Mostruario/MOSTRUARIO_ICONES_0.5.37.png`
- `LEIA-ME-PRIMEIRO.md` e `INDICE_DE_ORIGEM.csv`

**Validação:** pacote complementar = 133 arquivos, 1.081.147 bytes, SHA256 `aed8164510565d08b20096b434992ea677321ef81e69ad7278e38cff325a25e1`. Biblioteca integral V2 = 1.525 arquivos, 22.240.447 bytes, SHA256 `0d14696900ec3d7ec671fb8bf511b3fdd2975ff78fe8ae03c07403b72788b399`. Ambos os ZIPs passaram `ZipFile.testzip` local. **Entregas no Drive verificadas:** Pasta: https://drive.google.com/drive/folders/10svDEu4W1iCMkiO5attXCHduNEmW2NnP ; ZIP complementar: https://drive.google.com/file/d/1pzibhWTMq6Rknt23D5W2vbwncVD5ciyZ/view?usp=drivesdk ; biblioteca completa V2: https://drive.google.com/file/d/1xSfd7xzGQspC4etEJruMgF5ZXcs0VCKM/view?usp=drivesdk ; mostruário PNG: https://drive.google.com/file/d/1RbGH90QkKQ8rSLGN5-CZuTzKZurbKBes/view?usp=drivesdk . Upload confirmado via Google Drive. **Os arquivos individuais PNG/SVG dos ícones estão DENTRO do ZIP complementar**, não como arquivos independentes no Drive.

**Fidelidade:** os contornos dos 18 tipos vieram diretamente de `TypeGlyphPaths.kt` no ZIP original v0.5.10; as cores e a construção do círculo vieram de `BattleOverlayView.kt`. A versão atual v0.5.37 mantém essas rotinas. Os SVG/PNG são CONVERSÕES a partir do desenho programático, não arquivos PNG isolados que já existiam no APK. Algumas diferenças de fonte/antialiasing e cor podem surgir entre PNG e Canvas Android, e devem ser conferidas no aparelho.

**Prioridade na reconstrução 1.0.0:** incluir a pasta de origem com descrição/proveniência; usar apenas imagens aprovadas; não substituir ícones históricos por emoji e não confundir os badges do mockup com ranking real.

## Fontes oficiais
- [TypeGlyphPaths.kt](https://github.com/LucianoToscano/PvPPokeGo/blob/feature/home-mobile-0.5.37-20261013/app/src/main/java/com/lucianotoscano/pvppokego/overlay/TypeGlyphPaths.kt)
- [BattleOverlayView.kt](https://github.com/LucianoToscano/PvPPokeGo/blob/feature/home-mobile-0.5.37-20261013/app/src/main/java/com/lucianotoscano/pvppokego/overlay/BattleOverlayView.kt)
- [ArenaDashboard.kt](https://github.com/LucianoToscano/PvPPokeGo/blob/feature/home-mobile-0.5.37-20261013/app/src/main/java/com/lucianotoscano/pvppokego/ui/ArenaDashboard.kt)
- [Checklist de reconstrução](REBUILD_MASTER_CHECKLIST_1.0.0.md)
