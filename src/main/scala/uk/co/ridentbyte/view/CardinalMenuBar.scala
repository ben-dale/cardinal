package uk.co.ridentbyte.view

import javafx.scene.control.{Menu, MenuBar, MenuItem}

class CardinalMenuBar(showAsCurlCallback: () => Unit,
                      openCallback:() => Unit,
                      saveChangesToCurrentFileCallback: () => Unit,
                      saveAsCallback: () => Unit,
                      clearAllCallback: () => Unit,
                      editEnvironmentVarsCallback: () => Unit,
                      showFormUrlEncodedInputCallback: () => Unit,
                      showBasicAuthInputCallback: () => Unit) extends MenuBar {

  if (System.getProperty("os.name").toLowerCase.contains("mac")) {
    setUseSystemMenuBar(true)
  }

  // *** FILE ***
  private val menuFile = new Menu("File")

  val menuItemOpen = new MenuItem("Open...")
  menuItemOpen.setOnAction((_) => openCallback())
  menuFile.getItems.add(menuItemOpen)

  private val menuItemSave = new MenuItem("Save")
  menuItemSave.setDisable(true)
  menuItemSave.setOnAction((_) => saveChangesToCurrentFileCallback())
  menuFile.getItems.add(menuItemSave)

  private val menuItemSaveAs = new MenuItem("Save As...")
  menuItemSaveAs.setOnAction((_) => saveAsCallback())
  menuFile.getItems.add(menuItemSaveAs)

  private val menuItemClearAll = new MenuItem("Clear All")
  menuItemClearAll.setOnAction((_) => clearAllCallback())
  menuFile.getItems.add(menuItemClearAll)

  getMenus.add(menuFile)

  // *** TOOLS ***
  private val menuTools = new Menu("Tools")

  private val menuItemViewAsCurl = new MenuItem("View as cURL")
  menuItemViewAsCurl.setOnAction((_) => showAsCurlCallback())
  menuTools.getItems.add(menuItemViewAsCurl)

  getMenus.add(menuTools)

  // *** AUTHORISATION ***
  private val menuAuthorisation = new Menu("Auth")

  private val menuItemBasicAuth = new MenuItem("Basic Auth...")
  menuItemBasicAuth.setOnAction((_) => showBasicAuthInputCallback())
  menuAuthorisation.getItems.add(menuItemBasicAuth)

  getMenus.add(menuAuthorisation)

  // *** FORM ***
  private val menuForm = new Menu("Form")

  private val menuItemUrlEncoded = new MenuItem("URL Encoded...")
  menuItemUrlEncoded.setOnAction((_) => showFormUrlEncodedInputCallback())
  menuForm.getItems.add(menuItemUrlEncoded)

  getMenus.add(menuForm)

  // *** CONFIG ***
  private val menuConfig = new Menu("Config")
  private val menuItemEnvVars = new MenuItem("Environment Variables...")
  menuItemEnvVars.setOnAction((_) => editEnvironmentVarsCallback())
  menuConfig.getItems.add(menuItemEnvVars)
  getMenus.add(menuConfig)

  def setSaveDisabled(saveDisabled: Boolean): Unit = {
    menuItemSave.setDisable(saveDisabled)
  }

}
