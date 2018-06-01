package uk.co.ridentbyte.view

import javafx.scene.control.{Menu, MenuBar, MenuItem}

class CardinalMenuBar(newTab: () => Unit,
                      showAsCurl: () => Unit,
                      open:() => Unit,
                      save: (() => Unit) => Unit,
                      saveAs: (() => Unit) => Unit,
                      clearAll: () => Unit,
                      editEnvironmentVars: () => Unit,
                      showFormUrlEncodedInput: () => Unit,
                      showBasicAuthInput: () => Unit) extends MenuBar {

  if (System.getProperty("os.name").toLowerCase.contains("mac")) {
    setUseSystemMenuBar(true)
  }

  // *** FILE ***
  private val menuFile = new Menu("File")

  val menuItemNew = new MenuItem("New")
  menuItemNew.setOnAction(_ => newTab())
  menuFile.getItems.add(menuItemNew)

  val menuItemOpen = new MenuItem("Open...")
  menuItemOpen.setOnAction(_ => open())
  menuFile.getItems.add(menuItemOpen)

  private val menuItemSave = new MenuItem("Save")
  menuItemSave.setOnAction(_ => save(() => Unit))
  menuFile.getItems.add(menuItemSave)

  private val menuItemSaveAs = new MenuItem("Save As...")
  menuItemSaveAs.setOnAction(_ => saveAs(() => Unit))
  menuFile.getItems.add(menuItemSaveAs)

  private val menuItemClearAll = new MenuItem("Clear All")
  menuItemClearAll.setOnAction(_ => clearAll())
  menuFile.getItems.add(menuItemClearAll)

  getMenus.add(menuFile)

  // *** EXPORT ***
  private val menuExport = new Menu("Export")

  private val menuItemExportAsCurl = new MenuItem("Export as cURL")
  menuItemExportAsCurl.setOnAction(_ => showAsCurl())
  menuExport.getItems.add(menuItemExportAsCurl)

  getMenus.add(menuExport)

  // *** AUTHORISATION ***
  private val menuAuthorisation = new Menu("Auth")

  private val menuItemBasicAuth = new MenuItem("Basic Auth...")
  menuItemBasicAuth.setOnAction(_ => showBasicAuthInput())
  menuAuthorisation.getItems.add(menuItemBasicAuth)

  getMenus.add(menuAuthorisation)

  // *** FORM ***
  private val menuForm = new Menu("Form")

  private val menuItemUrlEncoded = new MenuItem("URL Encoded...")
  menuItemUrlEncoded.setOnAction(_ => showFormUrlEncodedInput())
  menuForm.getItems.add(menuItemUrlEncoded)

  getMenus.add(menuForm)

  // *** CONFIG ***
  private val menuConfig = new Menu("Config")
  private val menuItemEnvVars = new MenuItem("Edit Environment Variables...")
  menuItemEnvVars.setOnAction(_ => editEnvironmentVars())
  menuConfig.getItems.add(menuItemEnvVars)
  getMenus.add(menuConfig)

  def setSaveDisabled(saveDisabled: Boolean): Unit = {
    menuItemSave.setDisable(saveDisabled)
  }

}
