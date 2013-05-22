let file = findfile('syntax/actionscript.vim', escape(&runtimepath, ' '))
if file != ''
  let scriptFile = fnamemodify(file, ':p')
  exec "silent source " . scriptFile
endif

