# Biblioteca de imagens PvPPokeGo — APK 0.5.37 (13/10/2026)

Esta operação organizou imagens e não recompilou nem alterou o aplicativo.

## Localização oficial
- Pasta geral no Drive: https://drive.google.com/drive/folders/1zAEL_PhkrvXSyJ8twCWcQFKaybEtAyWI
- Backgrounds originais do usuário: https://drive.google.com/drive/folders/1qF1_Wyv8_x9MKAkbfcF4Tr10r9zeOXCp
- Referências de tela enviadas pelo usuário: https://drive.google.com/drive/folders/1RegKS5O87msOhHSZ_eHFtHIRnPyDsfZL
- Mockups gerados (NÃO são os backgrounds do usuário): https://drive.google.com/drive/folders/1ddyDnmkA_5mQkpnZbHkBk7QS6AozjNmX
- Ícones históricos 0.5.10: https://drive.google.com/drive/folders/1gmecKrsuLqsXBXUaSK4RaK2Ir1YefEZ_
- Extração no CI de imagens do APK: https://github.com/LucianoToscano/PvPPokeGo-APK-Build/actions/runs/38486949624

## Inventário verificado
- APK usado: 0.5.37 QA/debug (SHA256 0aa431a86f66d3cd6392330444fd3f307ef39378a28be2872aee6ea21360710d).
- 194 raster embutidos: 1 atlas de sprites Pokémon, 1 logo launcher, 192 imagens de dependências Android, várias duplicadas por densidade.
- Atlas de 1167 sprites individuais PNG 48x48 extraídos fielmente; atlas/index originais preservados.
- Atlas e launcher do APK 0.5.37 são idênticos aos do APK 0.5.36.
- Vetores Android XML binários preservados separadamente; não são PNGs ou fontes XML legíveis.
- Background original 1: 1000813346.png, bytes JPEG 1024x1536, fundo Home.
- Background original 2: 1000813369.png, bytes JPEG 864x1536, fundo Equipe/Arena.
- Os originais do usuário NÃO estão dentro do APK atual; foram copiados sem alterar bytes, renomeados no catálogo com extensão correta .jpg.
- 3 referências de telas originais e 3 conceitos gerados separados; não confundir.
- Pacote histórico de ícones 0.5.10 preservado; ícones de olho/cadeado e controles do HUD também são desenhados em Kotlin/Canvas e não necessariamente arquivos PNG.
- ZIP local completo: 1392 arquivos, 21.146.820 bytes, SHA256 cc33aeefff4fe3fc981467e8de7011a508c8bb8e8007f721379ae8bbc055f560.
- Biblioteca local inclui catálogo de busca HTML, 12 JPGs de mostruário e inventário CSV com SHA256.

## Regras da reconstrução
- Usar EXATAMENTE os dois fundos enviados, nunca similares da internet nem mockups gerados.
- Preservar artes e ícones originais e conferir licenças de sprites/recursos de terceiros.
- Não marcar fundo embutido em APK 1.0.0 até instalar/testar o novo build.
- Atualizar o Guia Mestre e o checklist de reconstrução ao incorporar cada imagem.
- O ZIP completo no chat contém os backgrounds externos; o pacote CI contém somente material do APK.

Checklist principal: REBUILD_MASTER_CHECKLIST_1.0.0.md