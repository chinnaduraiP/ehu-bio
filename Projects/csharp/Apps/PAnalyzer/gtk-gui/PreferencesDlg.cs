
// This file has been generated by the GUI designer. Do not modify.

public partial class PreferencesDlg
{
	private global::Gtk.VBox vbox2;
	private global::Gtk.Frame frame1;
	private global::Gtk.Alignment GtkAlignment2;
	private global::Gtk.Table table1;
	private global::Gtk.ComboBox DecoyCombo;
	private global::Gtk.Label label1;
	private global::Gtk.Label label3;
	private global::Gtk.Label label4;
	private global::Gtk.Label label5;
	private global::Gtk.Label label6;
	private global::Gtk.CheckButton PassThCheck;
	private global::Gtk.ComboBox PlgsThCombo;
	private global::Gtk.ComboBox RankThCombo;
	private global::Gtk.ComboBox SeqThCombo;
	private global::Gtk.Label GtkLabel2;
	private global::Gtk.Frame frame2;
	private global::Gtk.Alignment GtkAlignment3;
	private global::Gtk.Table table2;
	private global::Gtk.Label label2;
	private global::Gtk.ComboBox MultirunCombo;
	private global::Gtk.Label GtkLabel3;
	private global::Gtk.Button buttonCancel;
	private global::Gtk.Button button5;
	
	protected virtual void Build ()
	{
		global::Stetic.Gui.Initialize (this);
		// Widget PreferencesDlg
		this.Name = "PreferencesDlg";
		this.Title = global::Mono.Unix.Catalog.GetString ("Execution Preferences");
		this.Icon = global::Stetic.IconLoader.LoadIcon (this, "gtk-preferences", global::Gtk.IconSize.Menu);
		this.WindowPosition = ((global::Gtk.WindowPosition)(4));
		// Internal child PreferencesDlg.VBox
		global::Gtk.VBox w1 = this.VBox;
		w1.Name = "dialog1_VBox";
		w1.BorderWidth = ((uint)(5));
		// Container child dialog1_VBox.Gtk.Box+BoxChild
		this.vbox2 = new global::Gtk.VBox ();
		this.vbox2.Name = "vbox2";
		this.vbox2.Spacing = 6;
		// Container child vbox2.Gtk.Box+BoxChild
		this.frame1 = new global::Gtk.Frame ();
		this.frame1.Name = "frame1";
		this.frame1.ShadowType = ((global::Gtk.ShadowType)(2));
		this.frame1.LabelYalign = 0F;
		// Container child frame1.Gtk.Container+ContainerChild
		this.GtkAlignment2 = new global::Gtk.Alignment (0F, 0F, 1F, 1F);
		this.GtkAlignment2.Name = "GtkAlignment2";
		this.GtkAlignment2.LeftPadding = ((uint)(12));
		// Container child GtkAlignment2.Gtk.Container+ContainerChild
		this.table1 = new global::Gtk.Table (((uint)(5)), ((uint)(2)), false);
		this.table1.RowSpacing = ((uint)(6));
		this.table1.ColumnSpacing = ((uint)(20));
		this.table1.BorderWidth = ((uint)(12));
		// Container child table1.Gtk.Table+TableChild
		this.DecoyCombo = global::Gtk.ComboBox.NewText ();
		this.DecoyCombo.AppendText (global::Mono.Unix.Catalog.GetString ("Disabled"));
		this.DecoyCombo.AppendText (global::Mono.Unix.Catalog.GetString ("Filter"));
		this.DecoyCombo.Name = "DecoyCombo";
		this.DecoyCombo.Active = 0;
		this.table1.Add (this.DecoyCombo);
		global::Gtk.Table.TableChild w2 = ((global::Gtk.Table.TableChild)(this.table1 [this.DecoyCombo]));
		w2.TopAttach = ((uint)(4));
		w2.BottomAttach = ((uint)(5));
		w2.LeftAttach = ((uint)(1));
		w2.RightAttach = ((uint)(2));
		w2.XOptions = ((global::Gtk.AttachOptions)(4));
		w2.YOptions = ((global::Gtk.AttachOptions)(4));
		// Container child table1.Gtk.Table+TableChild
		this.label1 = new global::Gtk.Label ();
		this.label1.Name = "label1";
		this.label1.Xalign = 0F;
		this.label1.LabelProp = global::Mono.Unix.Catalog.GetString ("ProteinLynx Global SERVER:");
		this.table1.Add (this.label1);
		global::Gtk.Table.TableChild w3 = ((global::Gtk.Table.TableChild)(this.table1 [this.label1]));
		w3.XOptions = ((global::Gtk.AttachOptions)(4));
		w3.YOptions = ((global::Gtk.AttachOptions)(4));
		// Container child table1.Gtk.Table+TableChild
		this.label3 = new global::Gtk.Label ();
		this.label3.Name = "label3";
		this.label3.Xalign = 0F;
		this.label3.LabelProp = global::Mono.Unix.Catalog.GetString ("ProteomeDiscoverer/SEQUEST:");
		this.table1.Add (this.label3);
		global::Gtk.Table.TableChild w4 = ((global::Gtk.Table.TableChild)(this.table1 [this.label3]));
		w4.TopAttach = ((uint)(1));
		w4.BottomAttach = ((uint)(2));
		w4.XOptions = ((global::Gtk.AttachOptions)(4));
		w4.YOptions = ((global::Gtk.AttachOptions)(4));
		// Container child table1.Gtk.Table+TableChild
		this.label4 = new global::Gtk.Label ();
		this.label4.Name = "label4";
		this.label4.Xalign = 0F;
		this.label4.LabelProp = global::Mono.Unix.Catalog.GetString ("mzIdentML SpectrumIdentificationItem passThreshold:");
		this.table1.Add (this.label4);
		global::Gtk.Table.TableChild w5 = ((global::Gtk.Table.TableChild)(this.table1 [this.label4]));
		w5.TopAttach = ((uint)(2));
		w5.BottomAttach = ((uint)(3));
		w5.XOptions = ((global::Gtk.AttachOptions)(4));
		w5.YOptions = ((global::Gtk.AttachOptions)(4));
		// Container child table1.Gtk.Table+TableChild
		this.label5 = new global::Gtk.Label ();
		this.label5.Name = "label5";
		this.label5.Xalign = 0F;
		this.label5.LabelProp = global::Mono.Unix.Catalog.GetString ("mzIdentML SpectrumIdentificationItem rank:");
		this.table1.Add (this.label5);
		global::Gtk.Table.TableChild w6 = ((global::Gtk.Table.TableChild)(this.table1 [this.label5]));
		w6.TopAttach = ((uint)(3));
		w6.BottomAttach = ((uint)(4));
		w6.XOptions = ((global::Gtk.AttachOptions)(4));
		w6.YOptions = ((global::Gtk.AttachOptions)(4));
		// Container child table1.Gtk.Table+TableChild
		this.label6 = new global::Gtk.Label ();
		this.label6.Name = "label6";
		this.label6.Xalign = 0F;
		this.label6.LabelProp = global::Mono.Unix.Catalog.GetString ("mzIdentML PeptideEvidence isDecoy:");
		this.table1.Add (this.label6);
		global::Gtk.Table.TableChild w7 = ((global::Gtk.Table.TableChild)(this.table1 [this.label6]));
		w7.TopAttach = ((uint)(4));
		w7.BottomAttach = ((uint)(5));
		w7.XOptions = ((global::Gtk.AttachOptions)(4));
		w7.YOptions = ((global::Gtk.AttachOptions)(4));
		// Container child table1.Gtk.Table+TableChild
		this.PassThCheck = new global::Gtk.CheckButton ();
		this.PassThCheck.CanFocus = true;
		this.PassThCheck.Name = "PassThCheck";
		this.PassThCheck.Label = global::Mono.Unix.Catalog.GetString ("enable");
		this.PassThCheck.DrawIndicator = true;
		this.PassThCheck.UseUnderline = true;
		this.table1.Add (this.PassThCheck);
		global::Gtk.Table.TableChild w8 = ((global::Gtk.Table.TableChild)(this.table1 [this.PassThCheck]));
		w8.TopAttach = ((uint)(2));
		w8.BottomAttach = ((uint)(3));
		w8.LeftAttach = ((uint)(1));
		w8.RightAttach = ((uint)(2));
		w8.XOptions = ((global::Gtk.AttachOptions)(4));
		w8.YOptions = ((global::Gtk.AttachOptions)(4));
		// Container child table1.Gtk.Table+TableChild
		this.PlgsThCombo = global::Gtk.ComboBox.NewText ();
		this.PlgsThCombo.AppendText (global::Mono.Unix.Catalog.GetString ("Disabled"));
		this.PlgsThCombo.AppendText (global::Mono.Unix.Catalog.GetString ("Red"));
		this.PlgsThCombo.AppendText (global::Mono.Unix.Catalog.GetString ("Yellow"));
		this.PlgsThCombo.AppendText (global::Mono.Unix.Catalog.GetString ("Green"));
		this.PlgsThCombo.Name = "PlgsThCombo";
		this.PlgsThCombo.Active = 0;
		this.table1.Add (this.PlgsThCombo);
		global::Gtk.Table.TableChild w9 = ((global::Gtk.Table.TableChild)(this.table1 [this.PlgsThCombo]));
		w9.LeftAttach = ((uint)(1));
		w9.RightAttach = ((uint)(2));
		w9.XOptions = ((global::Gtk.AttachOptions)(4));
		w9.YOptions = ((global::Gtk.AttachOptions)(4));
		// Container child table1.Gtk.Table+TableChild
		this.RankThCombo = global::Gtk.ComboBox.NewText ();
		this.RankThCombo.AppendText (global::Mono.Unix.Catalog.GetString ("Disabled"));
		this.RankThCombo.AppendText (global::Mono.Unix.Catalog.GetString ("1"));
		this.RankThCombo.AppendText (global::Mono.Unix.Catalog.GetString ("2"));
		this.RankThCombo.AppendText (global::Mono.Unix.Catalog.GetString ("3"));
		this.RankThCombo.AppendText (global::Mono.Unix.Catalog.GetString ("4"));
		this.RankThCombo.Name = "RankThCombo";
		this.RankThCombo.Active = 0;
		this.table1.Add (this.RankThCombo);
		global::Gtk.Table.TableChild w10 = ((global::Gtk.Table.TableChild)(this.table1 [this.RankThCombo]));
		w10.TopAttach = ((uint)(3));
		w10.BottomAttach = ((uint)(4));
		w10.LeftAttach = ((uint)(1));
		w10.RightAttach = ((uint)(2));
		w10.XOptions = ((global::Gtk.AttachOptions)(4));
		w10.YOptions = ((global::Gtk.AttachOptions)(4));
		// Container child table1.Gtk.Table+TableChild
		this.SeqThCombo = global::Gtk.ComboBox.NewText ();
		this.SeqThCombo.AppendText (global::Mono.Unix.Catalog.GetString ("Disabled"));
		this.SeqThCombo.AppendText (global::Mono.Unix.Catalog.GetString ("Red"));
		this.SeqThCombo.AppendText (global::Mono.Unix.Catalog.GetString ("Yellow"));
		this.SeqThCombo.AppendText (global::Mono.Unix.Catalog.GetString ("Green"));
		this.SeqThCombo.Name = "SeqThCombo";
		this.SeqThCombo.Active = 0;
		this.table1.Add (this.SeqThCombo);
		global::Gtk.Table.TableChild w11 = ((global::Gtk.Table.TableChild)(this.table1 [this.SeqThCombo]));
		w11.TopAttach = ((uint)(1));
		w11.BottomAttach = ((uint)(2));
		w11.LeftAttach = ((uint)(1));
		w11.RightAttach = ((uint)(2));
		w11.XOptions = ((global::Gtk.AttachOptions)(4));
		w11.YOptions = ((global::Gtk.AttachOptions)(4));
		this.GtkAlignment2.Add (this.table1);
		this.frame1.Add (this.GtkAlignment2);
		this.GtkLabel2 = new global::Gtk.Label ();
		this.GtkLabel2.WidthRequest = 400;
		this.GtkLabel2.HeightRequest = 104;
		this.GtkLabel2.Name = "GtkLabel2";
		this.GtkLabel2.LabelProp = global::Mono.Unix.Catalog.GetString ("<b>PSSM/Peptide score thresholds</b>");
		this.GtkLabel2.UseMarkup = true;
		this.frame1.LabelWidget = this.GtkLabel2;
		this.vbox2.Add (this.frame1);
		global::Gtk.Box.BoxChild w14 = ((global::Gtk.Box.BoxChild)(this.vbox2 [this.frame1]));
		w14.Position = 0;
		w14.Expand = false;
		w14.Fill = false;
		// Container child vbox2.Gtk.Box+BoxChild
		this.frame2 = new global::Gtk.Frame ();
		this.frame2.Name = "frame2";
		this.frame2.ShadowType = ((global::Gtk.ShadowType)(2));
		this.frame2.LabelYalign = 0F;
		// Container child frame2.Gtk.Container+ContainerChild
		this.GtkAlignment3 = new global::Gtk.Alignment (0F, 0F, 1F, 1F);
		this.GtkAlignment3.Name = "GtkAlignment3";
		this.GtkAlignment3.LeftPadding = ((uint)(12));
		// Container child GtkAlignment3.Gtk.Container+ContainerChild
		this.table2 = new global::Gtk.Table (((uint)(1)), ((uint)(2)), false);
		this.table2.Name = "table2";
		this.table2.RowSpacing = ((uint)(6));
		this.table2.ColumnSpacing = ((uint)(20));
		this.table2.BorderWidth = ((uint)(12));
		// Container child table2.Gtk.Table+TableChild
		this.label2 = new global::Gtk.Label ();
		this.label2.Name = "label2";
		this.label2.Xalign = 0F;
		this.label2.LabelProp = global::Mono.Unix.Catalog.GetString ("Multi-run peptide threshold:");
		this.table2.Add (this.label2);
		global::Gtk.Table.TableChild w15 = ((global::Gtk.Table.TableChild)(this.table2 [this.label2]));
		w15.XOptions = ((global::Gtk.AttachOptions)(4));
		w15.YOptions = ((global::Gtk.AttachOptions)(4));
		// Container child table2.Gtk.Table+TableChild
		this.MultirunCombo = global::Gtk.ComboBox.NewText ();
		this.MultirunCombo.Name = "MultirunCombo";
		this.table2.Add (this.MultirunCombo);
		global::Gtk.Table.TableChild w16 = ((global::Gtk.Table.TableChild)(this.table2 [this.MultirunCombo]));
		w16.LeftAttach = ((uint)(1));
		w16.RightAttach = ((uint)(2));
		w16.XOptions = ((global::Gtk.AttachOptions)(4));
		w16.YOptions = ((global::Gtk.AttachOptions)(4));
		this.GtkAlignment3.Add (this.table2);
		this.frame2.Add (this.GtkAlignment3);
		this.GtkLabel3 = new global::Gtk.Label ();
		this.GtkLabel3.WidthRequest = 400;
		this.GtkLabel3.HeightRequest = 104;
		this.GtkLabel3.Name = "GtkLabel3";
		this.GtkLabel3.LabelProp = global::Mono.Unix.Catalog.GetString ("<b>Multi-run options</b>");
		this.GtkLabel3.UseMarkup = true;
		this.frame2.LabelWidget = this.GtkLabel3;
		this.vbox2.Add (this.frame2);
		global::Gtk.Box.BoxChild w19 = ((global::Gtk.Box.BoxChild)(this.vbox2 [this.frame2]));
		w19.Position = 1;
		w19.Expand = false;
		w19.Fill = false;
		w1.Add (this.vbox2);
		global::Gtk.Box.BoxChild w20 = ((global::Gtk.Box.BoxChild)(w1 [this.vbox2]));
		w20.Position = 0;
		w20.Expand = false;
		w20.Fill = false;
		// Internal child PreferencesDlg.ActionArea
		global::Gtk.HButtonBox w21 = this.ActionArea;
		w21.Name = "dialog1_ActionArea";
		w21.Spacing = 10;
		w21.BorderWidth = ((uint)(5));
		w21.LayoutStyle = ((global::Gtk.ButtonBoxStyle)(4));
		// Container child dialog1_ActionArea.Gtk.ButtonBox+ButtonBoxChild
		this.buttonCancel = new global::Gtk.Button ();
		this.buttonCancel.CanDefault = true;
		this.buttonCancel.CanFocus = true;
		this.buttonCancel.Name = "buttonCancel";
		this.buttonCancel.UseStock = true;
		this.buttonCancel.UseUnderline = true;
		this.buttonCancel.Label = "gtk-execute";
		this.AddActionWidget (this.buttonCancel, 0);
		global::Gtk.ButtonBox.ButtonBoxChild w22 = ((global::Gtk.ButtonBox.ButtonBoxChild)(w21 [this.buttonCancel]));
		w22.Expand = false;
		w22.Fill = false;
		// Container child dialog1_ActionArea.Gtk.ButtonBox+ButtonBoxChild
		this.button5 = new global::Gtk.Button ();
		this.button5.CanFocus = true;
		this.button5.Name = "button5";
		this.button5.UseStock = true;
		this.button5.UseUnderline = true;
		this.button5.Label = "gtk-cancel";
		this.AddActionWidget (this.button5, -6);
		global::Gtk.ButtonBox.ButtonBoxChild w23 = ((global::Gtk.ButtonBox.ButtonBoxChild)(w21 [this.button5]));
		w23.Position = 1;
		w23.Expand = false;
		w23.Fill = false;
		if ((this.Child != null)) {
			this.Child.ShowAll ();
		}
		this.DefaultWidth = 781;
		this.DefaultHeight = 526;
		this.Show ();
	}
}
