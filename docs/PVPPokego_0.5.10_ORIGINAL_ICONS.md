# PvPPokeGo 0.5.10 — original icons restored

## Versão
- versionCode: 15
- versionName: 0.5.10-original-icons-restored

## Objetivo
Restaurar o visual original delicado dos controles de olho e cadeado desenhados no Canvas.

As Views usadas para toque agora são transparentes e não desenham emoji, texto ou fundo. O visual fica exclusivamente em:
- BattleOverlayView.drawLayoutLock()
- BattleOverlayView.drawVisibilityToggle()

## Correções acumuladas
- touchSlop real do Android com mínimo de 16 px;
- arraste livre sem limite rígido de topo;
- zonas pequenas de arraste quando o cadeado está aberto, preservando toques do Pokémon GO fora dos blocos;
- anti-flicker: z-order não é reconstruído a cada redraw;
- barra de edição com ✓ e ↺;
- controles de olho/cadeado/menu disponíveis entre batalhas;
- EXTRA_APP_VISIBLE=false ao iniciar overlay;
- captura recriada somente quando a resolução realmente muda, ignorando jitter de 1–2 px;
- ajuda da tela de configurações atualizada para os ícones originais.

## Build
O workflow `.github/workflows/build-0.5.10-apk.yml` gera um APK Debug instalável e publica:
- APK;
- SHA-256.

## Regra visual
Não usar novamente emoji 🔒/🔓/👁/🙈/◉ como camada visual sobre o Canvas.
