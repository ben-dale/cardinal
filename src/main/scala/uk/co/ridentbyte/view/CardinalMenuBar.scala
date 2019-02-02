package uk.co.ridentbyte.view

import javafx.scene.control.{Menu, MenuBar, MenuItem}

class CardinalMenuBar(newTab: java.util.function.Function[Void, Void],
                      open: java.util.function.Function[Void, Void],
                      save: (() => Unit) => Unit,
                      saveAs: java.util.function.Function[Void, Void],
                      clearAll: java.util.function.Function[Void, Void],
                      editEnvironmentVars: java.util.function.Function[Void, Void],
                      showFormUrlEncodedInput: java.util.function.Function[Void, Void],
                      showBasicAuthInput: java.util.function.Function[Void, Void]) extends MenuBar {

  if (System.getProperty("os.name").toLowerCase.contains("mac")) {
    setUseSystemMenuBar(true)
  }

  // *** FILE ***
  private val menuFile = new Menu("File")

  val menuItemNew = new MenuItem("New")
  menuItemNew.setOnAction(_ => newTab.apply(null))
  menuFile.getItems.add(menuItemNew)

  val menuItemOpen = new MenuItem("Open...")
  menuItemOpen.setOnAction(_ => open.apply(null))
  menuFile.getItems.add(menuItemOpen)

  private val menuItemSave = new MenuItem("Save")
  menuItemSave.setOnAction(_ => save(() => Unit))
  menuFile.getItems.add(menuItemSave)

  private val menuItemSaveAs = new MenuItem("Save As...")
  menuItemSaveAs.setOnAction(_ => saveAs.apply(null))
  menuFile.getItems.add(menuItemSaveAs)

  private val menuItemClearAll = new MenuItem("Clear All")
  menuItemClearAll.setOnAction(_ => clearAll.apply(null))
  menuFile.getItems.add(menuItemClearAll)

  getMenus.add(menuFile)

  // *** AUTHORISATION ***
  private val menuAuthorisation = new Menu("Auth")

  private val menuItemBasicAuth = new MenuItem("Basic Auth...")
  menuItemBasicAuth.setOnAction(_ => showBasicAuthInput.apply(null))
  menuAuthorisation.getItems.add(menuItemBasicAuth)

  getMenus.add(menuAuthorisation)

  // *** FORM ***
  private val menuForm = new Menu("Form")

  private val menuItemUrlEncoded = new MenuItem("URL Encoded...")
  menuItemUrlEncoded.setOnAction(_ => showFormUrlEncodedInput.apply(null))
  menuForm.getItems.add(menuItemUrlEncoded)

  getMenus.add(menuForm)

  // *** CONFIG ***
  private val menuConfig = new Menu("Config")
  private val menuItemEnvVars = new MenuItem("Edit Environment Variables...")
  menuItemEnvVars.setOnAction(_ => editEnvironmentVars.apply(null))
  menuConfig.getItems.add(menuItemEnvVars)
  getMenus.add(menuConfig)

  def setSaveDisabled(saveDisabled: Boolean): Unit = {
    menuItemSave.setDisable(saveDisabled)
  }

}
