# PvPPokeGo

Assistente visual externo para batalhas PvP do Pokémon GO.

O projeto **não modifica Pokémon GO**, não injeta código, não acessa memória do jogo, não intercepta rede, não usa Accessibility para jogar e não envia cliques/comandos. Usa `MediaProjection` para captura visual e `TYPE_APPLICATION_OVERLAY` para desenhar informações por cima da tela.

## Fase atual

**Fase 1 — fundação segura do overlay.** O overlay é passivo/touch-through para não bloquear os controles do jogo. OCR de nome/PC e a estrutura visual já estão presentes; seleção manual por `▼` fica para a Fase 5, usando pequenas janelas touchable independentes.

Implementado: foreground service, MediaProjection, overlay transparente, OCR ML Kit em ROIs pequenas, modelos de batalha, energia 0–100 com energia residual, previsão de Fast Moves restantes, progresso circular, matchup, tipos fortes, configurações, escala, opacidade e DEBUG.

Ainda em validação/etapas futuras: reservas por reconhecimento visual, contagem automática de Fast Move, Charged Move confirmado, timer de troca, drag/edit e menus `▼`.

## Build automático

O workflow `.github/workflows/android-build.yml` compila o APK Debug e publica o artefato `PVPPokeGo-debug`.

## Crédito

Desenvolvimento PvPPokGo by: LucianoToscano  
(84) 99131-9021
