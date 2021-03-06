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
let s:command_project_build = "/project_build?type=<type>" " /project_build?type=<type>&all=<all>&project=<project1>&project=<project2>

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
function! vimide#project#impl#GetProject(...)
  let path = ''
  if a:0 > 0 
    let path = a:1
  endif

  if path == ''
    let path = expand('%:p')
  endif

  if path != ''
    let path = vimide#util#LegalPath(path, 2)
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
    call vimide#print#Echo("You're just focus in project: " . projectName)
  else
    call vimide#project#impl#UnableToDetermineProject()
  endif
endfunction

" ----------------------------------------------------------------------------
" Gets the project root directory path.
"
" GetProjectRoot:
"   [file]  - the file which in the project.
" ----------------------------------------------------------------------------
function! vimide#project#impl#GetProjectRoot(...)
  let file = ''
  if a:0 == 0
    let file = expand('%:p:h')
  elseif a:0 == 1 && a:1
    let file = a:1
  endif

  let project = vimide#project#impl#GetProjectInfo(file)
  if type(project) == g:DICT_TYPE
    return project.path
  endif
  return ''
endfunction

" ----------------------------------------------------------------------------
" Gets the project information object by the supplied file.
"
" GetProjectInfo:
"   file  - the file.
" ----------------------------------------------------------------------------
function! vimide#project#impl#GetProjectInfo(file)
  let projectName = vimide#project#impl#GetProject(a:file)
  let command = s:command_project_info
  let command = substitute(command, '<project>', projectName, '')

  let result = vimide#Execute(command)
  if type(result) == g:DICT_TYPE
    return result
  endif
  return ''
endfunction

" ----------------------------------------------------------------------------
" Build projects.
"
" ProjectBuild:
"   bang    - the specific bang to set for all projects.
"   cmdLine - the command line for building project.
" ----------------------------------------------------------------------------
function! vimide#project#impl#ProjectBuild(bang, cmdLine)
  " Sets the default build type to 9 (auto).
  let type = 9
  let typeName = 'auto'

  let args = vimide#util#ToList(a:cmdLine)

  if len(args) > 0
    if args[0] == '-t'
      if len(args) > 1
        let typeName = args[1]
      else
        return
      endif
      call remove(args, 0, 1)
    endif
  endif

  if typeName == 'full'
    let type = 6
  elseif typeName == 'increment'
    let type = 10
  elseif typeName == 'clean'
    let type = 11
  endif

  " starting to build phase.
  call vimide#print#Echo('Building projects...')

  let projects = []
  let all = 0
  if a:bang == '!'
    " for building all projects.
    let projects = vimide#project#impl#GetProjectNames()
    let all = 1
  else
    " if no project supplied, auto determines the current project by the
    " buffer file.
    if len(args) == 0
      let project = vimide#project#impl#GetProject(expand('%:p'))
      if project == ''
        call vimide#project#impl#UnableToDetermineProject()
        return
      endif
      silent! call add(projects, project)
    else
      for _arg in args
        if vimide#project#impl#IsProjectExists(_arg)
          silent! call add(projects, _arg)
        endif
      endfor
    endif
  endif

  if len(projects)
    let command = substitute(s:command_project_build, '<type>', type, '')
    if all
      let command .= '&all=1'
    else
      for project in projects
        let command .= '&project=' . project
      endfor
    endif

    let result = vimide#Execute(command)
    if type(result) == g:STRING_TYPE
      call vimide#print#Echo(result)
    endif
  else
    call vimide#print#EchoError("No illegal project was detected.")
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
    let project = vimide#project#impl#GetProject()
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
  call vimide#print#Echo('Requesting project information...')
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
" Determines the supplied project if exists.
"
" IsProjectExists:
"   project - the specific project to determine.
" ----------------------------------------------------------------------------
function! vimide#project#impl#IsProjectExists(project)
  if a:project != ''
    let projects = vimide#project#impl#GetProjectNames()
    let projects = filter(projects, 'v:val == "' . a:project . '"')
    return len(projects) > 0
  endif
  return 0
endfunction

" ----------------------------------------------------------------------------
" Determines the current buffer file if located in avaiable project.
"
" IsCurrentFileInProject:
" ----------------------------------------------------------------------------
function! vimide#project#impl#IsCurrentFileInProject(...)
  let project = vimide#project#impl#GetProject()
  return project == '' ? 0 : 1
endfunction

" ----------------------------------------------------------------------------
" Retrieves and print out the list of projects.
"
" ProjectList:
"   natures (optional) - the natures to filtering. sepa with comma.
" ----------------------------------------------------------------------------
function! vimide#project#impl#ProjectList(...)
  call vimide#print#Echo('Listing projects...')
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
    call vimide#print#Echo('Closing projects ... ')
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
    call vimide#print#Echo('Deleting projects ... ')
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
  call vimide#print#Echo('Importing projects ...')
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
    call vimide#print#Echo('Opening projects ...')
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
  call vimide#print#Echo('Refreshing projects ...')
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

" ----------------------------------------------------------------------------
" Custom command completion for project builds.
"
" CommandCompleteProjectBuild:
" ----------------------------------------------------------------------------
function! vimide#project#impl#CommandCompleteProjectBuild(argLead, cmdLine, cursorPos)
  let options = []
  " against no space for cmdline.
  if a:cmdLine !~ '\s'
    return options
  endif

  " determines all <bang> whether specified.
  if a:cmdLine !~ '^[0-9a-zA-Z_-]\+[!]'
    let projects = vimide#project#impl#CommandCompleteMultiProject(a:argLead, a:cmdLine, a:cursorPos)
    if a:cmdLine =~ '^[0-9a-zA-Z_-]\+\s\+$'
      call add(options, '-t')
      for _p in projects
        call add(options, _p)
      endfor
      return options
    elseif a:cmdLine =~ '^[0-9a-zA-Z_-]\+\s\+-t\s*\zs'
      let options = ['auto', 'increment', 'full', 'clean']
      " completation the value for type.
      let stype = substitute(a:cmdLine, '.* -t\s\+\(.\+\)', '\1', '')
      if stype != '' && stype != a:cmdLine
        call filter(options, 'v:val =~ "^' . stype . '"')
      endif
      if len(options) > 0
        return options
      endif
    endif

    return projects
  else
    " provided the type options.
    if a:cmdLine !~ '.* -t\s\+.*'
      return ['-t']
    endif

    " completation the value for type.
    let stype = substitute(a:cmdLine, '.* -t\s\+\(.\+\)', '\1', '')
    let options = ['auto', 'increment', 'full', 'clean']
    if stype != '' && stype != a:cmdLine
      call filter(options, 'v:val =~ "^' . stype . '"')
    endif
  endif

  return options
endfunction

" vim:ft=vim
