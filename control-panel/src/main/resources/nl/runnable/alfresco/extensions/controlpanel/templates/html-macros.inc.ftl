<#-- Macro that generates a Bootstrap document. -->
<#macro document title="" active="extensions">
<!DOCTYPE html>
<html>
  <head>
    <title>${title}</title>
    <base href="${url.serviceContext}/dynamic-extensions/"/>
    <link rel="stylesheet" type="text/css" href="resources/stylesheets/bootstrap/css/bootstrap.min.css"/>
    <link rel="stylesheet" type="text/css" href="resources/stylesheets/jasny-bootstrap/bootstrap-fileupload.css"/>
    <#-- <link rel="stylesheet" type="text/css" href="resources/stylesheets/bootstrap/css/bootstrap-responsive.min.css"/> -->
    <link rel="stylesheet" type="text/css" href="resources/stylesheets/sticky-footer.css"/>
    <link rel="stylesheet" type="text/css" href="resources/stylesheets/screen.css"/>
  </head>
  <body>
    <div id="wrap">
      <div class="navbar navbar-fixed-top">
        <div class="navbar-inner">
          <div class="container">
            <a class="brand" href="" title="Click to refresh">Dynamic Extensions for Alfresco</a>
            <ul class="nav">
              <li class="<#if (active == 'extensions')>active</#if>">
                <a href="">Extensions</a>
              </li>
              <li class="<#if (active == 'framework')>active</#if>">
                <a href="framework">Framework</a>
              </li>
            </ul>
            <ul class="nav pull-right">
              <li>
                <a href="https://github.com/lfridael/dynamic-extensions-for-alfresco" target="_blank">
                  Github project
                </a>
              </li>
            </ul>
          </div>
        </div>
      </div>
      <div class="container">
        <#if errorMessage??>
          <@alert type="error">
            ${errorMessage}
          </@alert>
        </#if>
        <#if successMessage??>
          <@alert type="success">
            ${successMessage}
          </@alert>
        </#if>
        <#nested/>
      </div>
      <div id="push"></div>
    </div>
    <div id="footer">
      <div class="container">
        <p class="last-updated">Last updated: <span id="last-updated">just now</span></p>
      </div>
    </div>
    <form id="post" method="post" target="postFrame" style="display: none;"></form>
    <iframe name="postFrame" style="display: none;"></iframe>
    <script type="text/javascript" src="resources/scripts/jquery/jquery-1.9.1.min.js"></script>
    <script type="text/javascript" src="resources/scripts/bootstrap/js/bootstrap.min.js"></script>
    <script type="text/javascript" src="resources/scripts/jasny-bootstrap/bootstrap-fileupload.js"></script>
    <script type="text/javascript" src="resources/scripts/bootbox/bootbox.min.js"></script>
    <script type="text/javascript" src="resources/scripts/moment/moment.min.js"></script>
    <script type="text/javascript" src="resources/scripts/control-panel.js"></script>
  </body>
</html>
</#macro>

<#macro bundleTable bundles>
  <table class="bundles table table-bordered table-striped">
    <thead>
      <tr>
        <th class="name">Bundle</th>
        <th class="status">Status</th>        
        <th class="modified">Modified</th>
        <th class="store">Store</th>
      </tr>      
    </thead>
    <tbody>
      <#list bundles as bundle>
        <tr class="<#if bundle.status == 'installed'>error</#if>">
          <td>
            <a href="bundles/${bundle.bundleId?string.computer}"
              data-trigger="hover"
              data-content="${bundle.description!}"
              data-delay="1000">
              ${bundle.name} ${bundle.version}
            </a>
          </td>
          <td>
            ${bundle.status}
          </td>
          <td>
            <span data-time="${bundle.lastModified?string.computer}"></span>
          </td>
          <td>
            ${bundle.store}
          </td>
        </tr>
      </#list>      
    </tbody>  
  </table>
</#macro>

<#macro alert type="success">
  <div class="alert alert-${type} alert-block">
    <button type="button" class="close" data-dismiss="alert">&times;</button>
    <#nested/>
  </div>
</#macro>
