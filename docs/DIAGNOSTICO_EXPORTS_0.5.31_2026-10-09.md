# PvPPokeGo 0.5.31 — diagnóstico baseado nos backups reais do aparelho (2026-10-09)

**Status:** análise estática dos **três arquivos fornecidos pelo usuário**, sem compilar Android, sem executar testes de código de produção, sem publicar APK ou modificar lógica de batalha.

## Fontes e limites

- Referências: `PvPPokeGo-0.5.31-config.json`, `PvPPokeGo-0.5.31-diagnostico.zip`, `PvPPokeGo-0.5.31-historico.zip`. Os originais vieram como anexos da conversa e NÃO foram copiados em bruto para este repositório, pois podem incluir configurações pessoais, composição de time e dados do dispositivo. Para reproduzir a análise, solicitar ao usuário novos anexos caso o chat não os forneça.
- SHA-256 respectivamente: `b018cee63a3784adb702e6042e7318951c16ceafed8604f4fdb34335324d23b0`, `61e60964d24b217d4c2c795891aede87022982f8a1d298d4c15d85e6d34e71da`, `7444a8b242106536913113ad59d824d2f3ed478aff2edf528dd705bac3c5e260`.
- Historico JSON em ambos os ZIPs **idêntico byte a byte**, SHA-256 `f395abee65fc32cb508c6279a1923a3ffca57e7ba0cc92f9abaa6bb717682242`.
- O histórico ZIP traz `historico.json`, `historico-legivel.txt`, `historico.csv`, `README.txt`. O diagnóstico ZIP traz `battle_history.json`, `settings.json`, `device.txt`, `README.txt`.
- Configuração separada: 34 chaves; cópia `settings.json` do diagnóstico: 35 chaves. **Única diferença**: `auto_recognition = false` adicional no diagnóstico. A ausência de flag no backup não comprova que o recurso ficou sempre habilitado ou desabilitado durante qualquer sessão; configuração pode ter sido alterada entre exportações.
- Aparelho informado em `device.txt`: Samsung, Android 16, 1080×2340, densidade 420dpi. Manter detalhes de dispositivo restritos a QA e não divulgar como identidade do proprietário.
- O manual_team_v1 configura três Pokémon com CP/IV/golpes; configurações/posições exatas de HUD devem ser **preservadas** e utilizadas apenas mediante consentimento do usuário para importar/reproduzir.

## Métricas verificadas por parser (e não por estimativa visual)

- 7 objetos de batalha e **702 eventos**; exportação tabular `historico.csv` possui exatamente 702 linhas de dados e sete IDs de batalha.
- 1 objeto com `result = Vitória`; **6 com `result = null`**. Não traduzir `null` em derrota, nem calcular estatística de vitórias como 1/7.
- Tipos de término: 3 por "Sem evidência de batalha por 28s", 2 por "Tela de seleção da equipe confirmada", 1 por "Nova partida confirmada por identidade visual/OCR" e 1 por "Resultado identificado na tela".
- Sessão de 3,7 s com `Vitória` e ambos os Pokémon ausentes; **nenhum evento OCR** na própria sessão. Outra sessão inicia ~9 s depois: possível fragmentação/resíduo de encerramento, mas **hipótese, não causa demonstrada**.
- Sessões de 31,9 s e 172,2 s sem jogadores/oponentes identificados; dessas, junto com a sessão de 3,7 s, total de **3 sessões sem nenhum evento OCR**.
- Maiores sessões com ~842,9 s e ~506,8 s; reportam várias trocas e muitas espécies aliadas/oponentes ao longo de uma só sessão (mais que três espécies de um lado). Isso exige verificar persistência indevida de sessão, filtros OCR, observações de troca e reconstrução de identidade.
- **34 eventos de TROCA com fonte `OCR` e rótulo `CONFIRMADO`**, mesmo quando o conjunto de Pokémon da sessão ultrapassa limites plausíveis. A palavra `CONFIRMADO` é classificação interna e NÃO equivale a verdade de terreno de um vídeo anotado.
- Em **4 das 7 sessões** há pelo menos um par de eventos em sequência fora da ordem ascendente de `elapsedMs`; investigar ordenação/tempo de inserção/relógio, mantendo ordenação estável para empates.
- Diagnóstico recorrente "Moveset das reservas" inclui campos `DESCONHECIDO` em algumas sessões. Não preencher golpes desconhecidos por suposição.
- A composição cadastrada em `manual_team_v1` (Garchomp/Houndoom/Cramorant) não coincide com todas as espécies registradas como aliadas (por exemplo, Tynamo/Regieleki/Yungoos). Essa divergência é **fato entre snapshots**, mas só um teste temporal da ativação do modo manual, da preferência global e da imagem real pode provar defeito específico.

## Orientação de correção — não concluída

**P0: Continuidade de batalha / segmentação**
- Identificar critérios de início/fim e reforçar invariantes de roster (até 3 aliados e 3 adversários por sessão).
- Evitar abrir um novo objeto ao detectar somente o resultado de uma partida que ainda tem contexto válido (reconciliar encerramento e janela de tolerância). Não juntar duas batalhas legítimas arbitrariamente.
- Só finalizar como vitória/derrota após evidência identificável; caso contrário usar `indeterminado` com razão/nível de confiança.
- Emitir eventos de "início novo" e "fim anterior" com motivo e evidência, permitindo auditoria.

**P0: Associação confiável da equipe / OCR**
- Testar a branch 0.5.32 (`TeamSetupRepository`, `ManualTeamMatchPolicy`, `BattleEngine.selectManualTeam`, `BattleCorrespondenceTracker`, `BattleOverlayService`) com esses casos, **sem alegar que a 0.5.32 já resolveu o problema**, pois ainda não foi executada no dispositivo.
- Separar quatro origens: roster manual `DECLARADO`, detecção visual `OCR`, inferência `ESTIMADO` e cenário de treino `SIMULADO`. Mostrar conflitos e não elevar hipóteses à certeza.
- Verificar que `auto_recognition=false` (chave do 0.5.31) e o novo modo `team_recognition_mode_v1` (da 0.5.32) são ajustes distintos. O backup 0.5.31 NÃO contém a chave `team_recognition_mode_v1`, comportamento esperado para versão anterior.

**P1: Integridade do histórico e análise**
- Corrigir classificação de sessões sem nome e sem OCR; contar de maneira separada `partida`, `sessão de observação`, `resultado verificado`, `fragmento/resíduo` e `indeterminado`.
- Adicionar `evidence` e `confidence` aos eventos decisivos; exibir no histórico o que foi reconhecido versus previsto.
- Ordenar `elapsedMs` no export ou usar sequência id/relógio monotônico preservando empates; assegurar que CSV, JSON e versão legível representem a mesma cronologia.
- Caso de teste: duas batalhas seguidas, reconhecimento desligado/ligado, tela de seleção intermédia, resultado seguido de nova partida, captura pausada, OCR adversário com múltiplas espécies, troca de reservas, roster Manual diferente do detectado e rotação/resolução.

## Estado do projeto e documentos

- Guia canônico consultado: https://github.com/LucianoToscano/PvPPokeGo/blob/release/0.5.29/docs/PROJECT_CONTINUITY_GUIDE.md
- Histórico: https://github.com/LucianoToscano/PvPPokeGo/blob/release/0.5.26/docs/PROJECT_CONTINUITY_GUIDE.md
- Branch de desenvolvimento `feature/team-recognition-modes-0.5.32` verificada antes do registro; **não utilizar o backup 0.5.31 para afirmar status funcional da 0.5.32**.
- Checklist de QA 0.5.32 tem testes **escritos, não executados**, para regras de associação manual/automática.
- `docs/PROJECT_CONTINUITY_GUIDE.md` e `docs/CHECKLIST-PvPPokeGo-0.5.29.md` devem continuar acompanhando este documento.
- **Nenhum build/CI/APK/teste Kotlin iniciado neste ciclo. Sem upload dos arquivos brutos para repositório público ou Drive.**
