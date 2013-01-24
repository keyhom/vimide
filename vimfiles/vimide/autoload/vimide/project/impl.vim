" Vimide Script.
" Author: keyhom (keyhom.c@gmail.com)
" License: Copyright (c) 2012 keyhom.c@gmail.com.
" 
"   This software is provided 'as-is', without any express or implied warranty.
"   In no event will the authors be held liable for any damages arising from
"   the use of this software.
"   
"   Permission is granted to anyone to use this software for any purpose
"   excluding commercial applications, and to alter it and redistribute it
"   freely, subject to the following restrictions:
"   
"     1. The origin of this software must not be misrepresented; you must not
"     claim that you wrote the original software. If you use this software
"     in a product, an acknowledgment in the product documentation would be
"     appreciated but is not required.
"   
"     2. Altered source versions must be plainly marked as such, and must not
"     be misrepresented as being the original software.
"   
"     3. This notice may not be removed or altered from any source
"     distribution.
" 

" ----------------------------------------------------------------------------
"
" Script Variables:
"
" ----------------------------------------------------------------------------

let s:command_project_by_resource = "/project_by_resource?file=<file>"
let s:command_project_list = "/project_list"
let s:command_project_names = "/project_names"
let s:command_project_info = "/project_info?project=<project>"
let s:command_project_close = "/project_close"
let s:command_project_open = "/project_open"
let s:command_project_import = "/project_import?file=<file>"
let s:command_project_delete = "/project_delete"
let s:command_project_refresh = "/project_refresh"
let s:command_project_refresh_File = "/project_refresh_file?project=<project>&file=<file>"

" ----------------------------------------------------------------------------
"
" Functions:
"
" ----------------------------------------------------------------------------

" ----------------------------------------------------------------------------
" Retrieves the project name of current project by the specified file path.
"
" GetProject:
"   path  - the specific path located in the target project.
" ----------------------------------------------------------------------------
function! vimide#project#impl#GetProject(path)
  if a:path != ''
    let path = vimide#util#LegalPath(a:path)
    let command = substitute(s:command_project_by_resource, '<file>', path, '')
    let result = vimide#Execute(command)
    if string(result) != ''
      return result
    endif
  endif
  return ''
endfunction

" ----------------------------------------------------------------------------
" Retrieves the name of current project and print it out.
"
" PrintCurrentProjectName:
" ----------------------------------------------------------------------------
function! vimide#project#impl#PrintCurrentProjectName()
  let projectName = vimide#project#impl#GetProject(expand('%:p'))
  if projectName != ''
    call vimide#print#EchoInfo("You're just focus in project: " . projectName)
  else
    call vimide#project#impl#UnableToDetermineProject()
  endif
endfunction

" ----------------------------------------------------------------------------
" Change dir to the specific project location.
"
" ProjectLCD:
"   project (optional) - the specific project to forward.
" ----------------------------------------------------------------------------
function! vimide#project#impl#ProjectLCD(...)
  let project = ''
  if a:0 > 0
    let project = a:1
  else
    let project = vimide#project#impl#GetProject(expand('%:p'))
  endif

  if project != ''
    let command = substitute(s:command_project_info, '<project>', project, '')
    let result = vimide#Execute(command)
    if type(result) == g:DICT_TYPE
      let location = vimide#util#LegalPath(result.path)
      if location != ''
        exec 'lcd ' . location
      else
        call vimide#print#EchoError("Unable to determine the location of the project: " . a:project)
      endif
    endif
  else
    call vimide#print#EchoError("Please supply the specific project name to chdir.")
  endif
endfunction

" ----------------------------------------------------------------------------
" Retrieves and print out the supplied or focus project informations.
"
" ProjectInfo:
"   project - the specific project to determine.
" ----------------------------------------------------------------------------
function! vimide#project#impl#ProjectInfo(project)
  let project = ''
  let _arr = split(a:project, '\s')

  if len(_arr) > 0
    let project = _arr[0]
  endif
  
  if project == ''
    let project = vimide#project#impl#GetProject(expand('%:p'))
  endif
  if project == ''
    call vimide#project#impl#UnableToDetermineProject()
    return
  endif

  let command = substitute(s:command_project_info, '<project>', project, '')
  let result = vimide#Execute(command)

  if type(result) == g:DICT_TYPE
    let output = 
          \ 'Name:      ' . result.name . "\n" . 
          \ 'Path:      ' . result.path . "\n" .
          \ 'Workspace: ' . result.workspace . "\n" .
          \ 'Open:      ' . result.open
    if has_key(result, 'natures')
      let output .= "\n" . 'Natures:   ' . join(result.natures, ', ')
    endif
    if has_key(result, 'depends')
      let output .= "\n" . 'Depends:   ' . join(result.depends, ', ')
    endif
    if has_key(result, 'referenced')
      let output .= "\n" . 'Referenced:' . join(result.referenced, ', ')
    endif
    call vimide#print#Echo(output)
  elseif type(result) == g:STRING_TYPE
    call vimide#print#Echo(result)
  endif
endfunction

" ----------------------------------------------------------------------------
" Gets the name collection of the projects.
"
" GetProjectNames:
" ----------------------------------------------------------------------------
function! vimide#project#impl#GetProjectNames()
  let command = s:command_project_names
  let result = vimide#Execute(command)
  if type(result) == g:LIST_TYPE
    return result
  else
    call vimide#print#EchoError('Error format.')
  endif
  return []
endfunction

" ----------------------------------------------------------------------------
" Retrieves and print out the list of projects.
"
" ProjectList:
"   natures (optional) - the natures to filtering. sepa with comma.
" ----------------------------------------------------------------------------
function! vimide#project#impl#ProjectList(...)
  let command = s:command_project_list

  let queryString = ''
  let natures = []

  if a:0 > 0
    let natures = split(a:1, ',')
  endif

  for _nature in natures
    if strlen(queryString)
      let queryString .= '&'
    endif
    let queryString .= 'natures=' . _nature
  endfor

  " specific the natures.
  if queryString != ''
    let command .= '?' . queryString
  endif

  let result = vimide#Execute(command)
  if type(result) == g:LIST_TYPE
    " [{
    "   project name:
    "   open status :
    "   project path:
    " }]

    if len(result) == 0
      call vimide#print#EchoInfo("No projects with natures.")
      return
    endif

    let messages = ''
    let index = 1

    let table = []

    for info in result
      let row = []
      let name = get(info, 'name')
      let open = get(info, 'open')
      let path = get(info, 'path')

      silent! call add(row, index)
      silent! call add(row, name)
      silent! call add(row, open == 'true' ? '[Opened]' : '[Closed]')
      silent! call add(row, path)
      silent! call add(table, row)

      let index = index + 1
    endfor

    let table = vimide#util#PadTable(table)

    for row in table
      let messages .= row[0] . ': ' . row[1] . ' ' . row[2] . ' => ' . row[3] . "\n"
    endfor

    call vimide#print#EchoInfo(messages)
  else
    call vimide#print#EchoError('Error format.')
  endif
endfunction

" ----------------------------------------------------------------------------
" Closes the supplied project.
"
" ProjectClose:
"   projects[...] - the specific projects to close.
" ----------------------------------------------------------------------------
function! vimide#project#impl#ProjectClose(...)
  if a:0 > 0
    let queryString = ''
    for _p in a:000
      if strlen(queryString)
        let queryString .= '&'
      endif
      let queryString .= 'project=' . _p
    endfor

    let command = s:command_project_close . '?' . queryString
    let result = vimide#Execute(command)
    if type(result) == g:STRING_TYPE
      call vimide#print#Echo(result)
    endif
  endif
endfunction

" ----------------------------------------------------------------------------
" Deletes the supplied projects.
"
" ProjectDelete:
"   projects[...] - the specific projects to delete.
" ----------------------------------------------------------------------------
function! vimide#project#impl#ProjectDelete(...)
  if a:0 > 0
    let queryString = ''
    for _p in a:000
      if strlen(queryString)
        let queryString .= '&'
      endif
      let queryString .= 'project=' . _p
    endfor
    let command = s:command_project_delete . '?' . queryString
    let result = vimide#Execute(command)
    if type(result) == g:STRING_TYPE
      call vimide#print#Echo(result)
    endif
  endif
endfunction

" ----------------------------------------------------------------------------
" Imports the project locate at the supplied path.
"
" ProjectImport:
"   path  - the path of the specific project located at.
" ----------------------------------------------------------------------------
function! vimide#project#impl#ProjectImport(path)
  let path = fnamemodify(expand(a:path), ':p:h')
  if path != ''
    let path = vimide#util#LegalPath(path, 2)
    let command = substitute(s:command_project_import, '<file>', path, '')
    let result = vimide#Execute(command)
    if type(result) == g:STRING_TYPE
      call vimide#print#Echo(result)
    else
      call vimide#print#Echo("Unexpected result response format.")
    endif
  endif
endfunction

" ----------------------------------------------------------------------------
" Opens the supplied project.
"
" ProjectOpen:
"   projects[...] - the specific projects to open.
" ----------------------------------------------------------------------------
function! vimide#project#impl#ProjectOpen(...)
  if a:0 > 0
    let queryString = ''
    for _p in a:000
      if strlen(queryString)
        let queryString .= '&'
      endif
      let queryString .= 'project=' . _p
    endfor
    let command = s:command_project_open . '?' . queryString
    let result = vimide#Execute(command)
    if type(result) == g:STRING_TYPE
      call vimide#print#Echo(result)
    endif
  endif
endfunction

" ----------------------------------------------------------------------------
" Refreshs the supplied projects.
"
" ProjectRefresh:
"   bang          - the specific bang to set for all.
"   projects[...] - the specific projects to refresh.
" ----------------------------------------------------------------------------
function! vimide#project#impl#ProjectRefresh(bang, ...)
  let projects = []
  if a:bang == '!'
    let projects = vimide#project#impl#GetProjectNames()
  else
    let projects = copy(a:000)
    if a:0 == 0
      " determines the current project by auto.
      let _p = vimide#project#impl#GetProject(expand('%:p'))
      if _p == ''
        " can't determines the current project.
        call vimide#project#impl#UnableToDetermineProject()
        return
      endif
      call add(projects, _p)
    endif
  endif

  if len(projects) > 0
    let queryString = ''
    for _p in projects
      if strlen(queryString)
        let queryString .= '&'
      endif
      let queryString .= 'project=' . _p
    endfor
    let command = s:command_project_refresh . '?' . queryString
    let result = vimide#Execute(command)
    if type(result) == g:STRING_TYPE
      call vimide#print#Echo(result)
    endif
  endif
endfunction

" ----------------------------------------------------------------------------
" Refresh the supplied project file. This function will just send a sync
" request to the server and having empty response.
"
" ProjectRefreshFile:
"   project - the specific project that the file was located at.
"   file    - the specific file to refresh.
" ----------------------------------------------------------------------------
function! vimide#project#impl#ProjectRefreshFile(project, file)
  if a:project != '' && a:file != ''
    let file = vimide#util#LegalPath(a:file)
    let command = substitute(s:command_project_refresh_File, '<project>', a:project, '')
    let command = substitute(command, '<file>', file, '')
    silent! call vimide#Execute(command)
  endif
endfunction

" ----------------------------------------------------------------------------
" Error handling for Unable to determines the supplied project.
"
" UnableToDetermineProject:
" ----------------------------------------------------------------------------
function! vimide#project#impl#UnableToDetermineProject()
  call vimide#print#EchoError("Unable to determine the project. " . 
        \ "Please specify a project name or " . 
        \ "execute from a valid project directory.")
endfunction

" ----------------------------------------------------------------------------
"
" Command Completion:
"
" ----------------------------------------------------------------------------

" ----------------------------------------------------------------------------
" Custom command completion for project names.
"
" CommandCompleteProject:
" ----------------------------------------------------------------------------
function! vimide#project#impl#CommandCompleteProject(argLead, cmdLine, cursorPos)
  return vimide#project#impl#CommandCompleteProjectByNature(a:argLead, 
        \ a:cmdLine, a:cursorPos, '')
endfunction

" ----------------------------------------------------------------------------
" Custom command completion for single project name only.
"
" CommandCompleteSingleProject:
" ----------------------------------------------------------------------------
function! vimide#project#impl#CommandCompleteSingleProject(argLead, cmdLine, cursorPos)
  let cmdLine = strpart(a:cmdLine, 0, a:cursorPos)
  let cmdTail = strpart(a:cmdLine, a:cursorPos)
  let argLead = substitute(a:argLead, cmdTail . '$', '', '')

  let projects = vimide#project#impl#GetProjectNames()

  " block when had select a project name.
  let args = split(cmdLine, '\s')

  if len(args) > 1
    for _project in projects
      if args[1] =~ _project
        return []
      endif
    endfor
  endif

  if cmdLine !~ '[^\\]\s$'
    let argLead = escape(escape(argLead, '~'), '~')
    " remove escape slashes
    let argLead = substitute(argLead, '\', '', 'g')
    call filter(projects, 'v:val =~ "^' . argLead . '"')
  endif

  call map(projects, 'escape(v:val, " ")')
  return projects
endfunction

" ----------------------------------------------------------------------------
" Custom command completion for multi projects names.
"
" CommandCompleteMultiProject:
" ----------------------------------------------------------------------------
function! vimide#project#impl#CommandCompleteMultiProject(argLead, cmdLine, cursorPos)
  let cmdLine = strpart(a:cmdLine, 0, a:cursorPos)
  let cmdTail = strpart(a:cmdLine, a:cursorPos)
  let argLead = substitute(a:argLead, cmdTail . '$', '', '')

  let projects = vimide#project#impl#GetProjectNames()

  " filtering the selected project name.
  let args = split(cmdLine, '\s')

  if len(args) > 1
    for _a in args
      call filter(projects, 'v:val != "' . _a . '"')
    endfor
  endif

  if cmdLine !~ '[^\\]\s$'
    let argLead = escape(escape(argLead, '~'), '~')
    " remove escape slashes
    let argLead = substitute(argLead, '\', '', 'g')
    call filter(projects, 'v:val =~ "^' . argLead . '"')
  endif

  call map(projects, 'escape(v:val, " ")')
  return projects
endfunction

" ----------------------------------------------------------------------------
" Custom command completion for project names by natures.
"
" CommandCompleteProjectByNature:
" ----------------------------------------------------------------------------
function! vimide#project#impl#CommandCompleteProjectByNature(argLead, cmdLine, cursorPos, nature)
  let cmdLine = strpart(a:cmdLine, 0, a:cursorPos)
  let cmdTail = strpart(a:cmdLine, a:cursorPos)
  let argLead = substitute(a:argLead, cmdTail . '$', '', '')

  let projects = vimide#project#impl#GetProjectNames()
  if cmdLine !~ '[^\\]\s$'
    let argLead = escape(escape(argLead, '~'), '~')
    " remove escape slashes
    let argLead = substitute(argLead, '\', '', 'g')
    call filter(projects, 'v:val =~ "^' . argLead . '"')
  endif

  call map(projects, 'escape(v:val, " ")')
  return projects
endfunction

" vim:ft=vim
