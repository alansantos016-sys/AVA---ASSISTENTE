# AVA Assistente

Aplicativo Android em desenvolvimento para apoiar a navegação do aluno no AVA Anhanguera, com autenticação realizada pelo próprio aluno.

## Escopo inicial
- Abrir o portal oficial dentro de um WebView.
- Permitir login do aluno diretamente na página institucional.
- Navegação básica: voltar, atualizar e abrir o AVA.
- Não coletar nem armazenar senha do aluno.
- Identificar, quando disponíveis na página, tópicos, parágrafos e vídeo.
- Caderno local de estudos para anotações e copiar material/pedido de resumo.

## Limites e segurança
A automação futura deve respeitar os termos da instituição e não contornar mecanismos de segurança, presença ou avaliações. Para questionários avaliativos, a versão inicial oferece suporte de estudo e revisão, mantendo o aluno responsável por responder e enviar.

## Gerar APK pelo GitHub
O workflow **Build Android APK** compila o APK de depuração quando há atualização na branch `main`, em um pull request ou quando é acionado manualmente.

1. Abra a aba **Actions** deste repositório.
2. Selecione **Build Android APK**.
3. Abra a execução mais recente e aguarde o término do job.
4. Em **Artifacts**, baixe `ava-assistente-debug-apk`.
5. Extraia o arquivo ZIP. O APK se chama `app-debug.apk`.

O APK é de depuração e não está assinado para publicação na Play Store. O fluxo automatizado ainda precisa executar com sucesso para confirmar a compilação.

## Construir localmente
Abra o projeto no Android Studio, sincronize Gradle e compile para um dispositivo Android. A implementação de início está em `app/`.

## Status
Protótipo inicial. A integração e compatibilidade do login WebView ainda precisam ser testadas no dispositivo; alguns provedores podem exigir navegador externo ou fluxo oficial de autenticação.
