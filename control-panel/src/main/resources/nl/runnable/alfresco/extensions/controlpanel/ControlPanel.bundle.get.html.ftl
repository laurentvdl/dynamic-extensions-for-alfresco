<#import "templates/html-macros.inc.ftl" as html>
<#if bundle.dynamicExtension>
  <#assign active="extensions" />
<#else>
  <#assign active="framework" />
</#if>
<@html.document title="${bundle.name} ${bundle.version} - Dynamic Extensions" active="${active}">
  <#macro header name value="">
    <tr>
      <th class="name">
        ${name}
      </th>
      <td class="value">
        <#nested />
      </td>
    </tr>
  </#macro>

  <h2>${bundle.name} ${bundle.version}</h2>

  <table class="bundle table table-striped table-bordered">
    <@header name="Type">
      <#if bundle.dynamicExtension>
        Extension bundle
      <#else>
        Framework bundle
      </#if>
    </@header>
    <@header name="Description">${bundle.description!""}</@header>
    <@header name="Symbolic Name">${bundle.symbolicName}</@header>
    <@header name="Location">
      ${bundle.location!""}
    </@header>
    <@header name="Status">${bundle.status}</@header>
    <@header name="Modified">
      <span data-time="${bundle.lastModified?string.computer}"></span>
    </@header>
  </table>

  <#if bundle.deleteable>
    <div class="uninstall">
      <form action="delete-bundle" method="post" 
        data-title="Delete Bundle"
        data-confirm="Are you sure you want to delete this bundle?<p>This removes the bundle from the repository.">
        <input type="hidden" name="id" value="${bundle.bundleId?string.computer}" />
        <button class="btn btn-danger">Delete Bundle</button>
      </form>
    </div>
  </#if>

  <#if bundle.status == 'installed' || bundle.status == 'resolved'>
    <div class="start">
      <form action="start-bundle" method="post"
          data-title="Start Bundle">
        <input type="hidden" name="id" value="${bundle.bundleId?string.computer}" />
        <button class="btn">Start Bundle</button>
      </form>
    </div>
	</#if>

  <#if bundle.status == 'installed'>
    <div class="alert alert-error alert-block">
      <p>
        If this bundle remains in the <code>installed</code> state, the Java packages that 
        this bundle depends on have not all been resolved. (If the OSGi framework is currently 
        restarting this page may not reflect the latest status.)
      </p>
      <p>
        Java packages can be imported from:
      </p>
      <ul>
        <li>
          <a href="bundles/0">The System Bundle</a>
          <br/>
          Provides access to packages that comprise the Java platform and the Alfresco repository. 
        </li>
        <li>
          <a href="framework">Framework bundles</a>
          <br/>
          These form the core Dynamic Extensions infrastructure.
        </li>
        <li>
          <a href="">Other Dynamic Extensions</a>
          <br/>
          Extensions may export their packages and services for use by other extensions.
        </li>
      </ul>
      <p>
        Once the dependencies are available, this bundle's state should change to
        <code>resolved</code> or <code>active</code>.
      </p>
      <p>
        <em>
          The bundle may have missing <code>Import-Bundle</code> or <code>Import-Library</code> dependencies.
          These types of dependency are generally not be used with Dynamic Extensions.
        </em>
      </p>
      <p>
          Try to start the bundle to obtain more specific information on why it is not resolved.
      </p>
    </div>
  </#if>

  <#if (bundle.bundleId != 0)>
    <h2>Imported packages</h2>  
    <#if (bundle.importedPackages?size > 0)>
      <table class="imported package table table-striped table-bordered">
        <thead>
          <tr>
            <th class="name">Imported Java Package</th>
            <th class="min version">Min Version</th>
            <th class="max version">Max Version</th>
          </tr>
        </thead>
        <tbody>
          <#list bundle.importedPackages as package>
            <tr>
              <td class="name">${package.name}</td>
              <td class="min version">${package.minVersion!'unbounded'}</td>
              <td class="max version">${package.maxVersion!'unbounded'}</td>
            </tr>
          </#list>
        </tbody>
      </table>
    <#else>
      <p>
        This bundle does not import any packages.
      </p>
    </#if>

    <h2>Exported packages</h2>
    <#if (bundle.exportedPackages?size > 0)>
      <table class="package table table-striped table-bordered">
        <thead>
          <tr>
            <th class="name">Exported Java Package</th>
            <th class="version">Version</th>
          </tr>
        </thead>
        <tbody>
          <#list bundle.exportedPackages as package>
            <tr>
              <td class="name">${package.packageName}</td>
              <td class="version">${package.version!''}</td>
            </tr>
          </#list>
        </tbody>
      </table>
    <#else>
    <p>
      This bundle does not export any packages.
    </p>
    </#if>
  <#else>
    <p><em>Displaying Java package information for the Framework bundle is currently not supported.</em></p>
  </#if>

</@html.document>
