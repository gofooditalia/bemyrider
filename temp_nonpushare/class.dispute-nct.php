<?php

class dispute extends Home {

    public $data = array();

    public function __construct($module, $id = 0, $objPost = NULL, $searchArray = array(), $type = '') {
        global $fields, $sessCataId;

        $this->data['id'] = $this->id = $id;
        $this->fields = $fields;
        $this->module = $module;
        $this->table = 'tbl_service_master';

        $this->formtype = ($this->id > 0 ? 'edit' : 'add');
        $this->searchArray = $searchArray;
        parent::__construct();

        $dispute_sql = "select d.dispute_id, d.dispute_title, d.status, d.customer_id, d.createdUser,
                d.provider_id, d.service_id, d.dispute_infavor, d.createdDate, sm.service_name, u.firstName, u.lastName
                    FROM tbl_dispute_master d
                    INNER JOIN tbl_users u on u.id = d.createdUser
                    INNER JOIN tbl_services s on s.id = d.service_id
                    INNER JOIN tbl_service_master sm on sm.service_id = s.service_id
                    where d.dispute_id = ? ";

        $disput_where = array($this->id);
        $categories = $this->pdoQuery($dispute_sql, $disput_where)->result();

        if ($this->id > 0) {
            if ($categories['customer_id'] == $categories['createdUser'])
                $this->created_by = "Customer";
            else
                $this->created_by = "provider";


            $message_sql = "select * from tbl_dispute_messages where dispute_id = ? ";
            $message_where = array($categories['dispute_id']);
            $messages = $this->pdoQuery($message_sql, $message_where)->results();
            $message_str = '';

            foreach ($messages as $message) {
                if ($message['createdUser'] == $categories['customer_id'])
                    $message_str .= "<b>Customer</b> : " . $message['dispute_message'] . "<br/>";
                else
                    $message_str .= "<b>provider</b> : " . $message['dispute_message'] . "<br/>";
            }

            $this->data['dispute_id'] = $this->dispute_id = filtering($categories['dispute_id']);
            $this->data['dispute_messages'] = $this->dispute_messages = $message_str;
            $this->data['dispute_title'] = $this->dispute_title = filtering($categories['dispute_title']);
            $this->data['raise_by'] = $this->raise_by = filtering($categories['firstName']) . " " . filtering($categories['lastName']);
            $this->data['raise_date'] = $this->raise_date = date("d M Y h:i A", strtotime($categories['createdDate']));
            $this->data['status'] = $this->status = filtering($categories['status']);
            $this->data['service_name'] = $this->service_name = filtering($categories['service_name']);
            $this->data['dispute_infavor'] = $this->dispute_infavor = $categories['dispute_infavor'];
            $this->data['customer_id'] = $this->customer_id = $categories['customer_id'];
        }
        else {
            $this->data['dispute_id'] = $this->dispute_id = "0";
            $this->data['dispute_messages'] = $this->dispute_messages = "";
            $this->data['dispute_title'] = $this->dispute_title = "";
            $this->data['status'] = $this->status = "";
            $this->data['service_name'] = $this->service_name = "";
            $this->data['raise_by'] = $this->raise_by = "";
            $this->data['raise_date'] = $this->raise_date = "";
            $this->data['dispute_infavor'] = $this->dispute_infavor = '0';
        }

        switch ($type) {
            case 'add' : {
                    $this->data['content'] = (in_array('add', $this->Permission)) ? $this->getForm() : '';
                    break;
                }
            case 'edit' : {
                    $this->data['content'] = (in_array('edit', $this->Permission)) ? $this->getForm() : '';
                    break;
                }
            case 'view': {
                    $this->data['content'] = (in_array('view', $this->Permission)) ? $this->viewForm() : '';
                    break;
                }
            case 'delete' : {
                    $this->data['content'] = (in_array('delete', $this->Permission)) ? json_encode($this->dataGrid()) : '';
                    break;
                }
            case 'datagrid' : {
                    $this->data['content'] = (in_array('module', $this->Permission)) ? json_encode($this->dataGrid()) : '';
                }
        }
    }

    public function viewForm() {

        $dispute_str = "";

        $dispute_str .= "<h3>Dispute Information : </h3>";
        $dispute_str .= $this->displayBox(array("label" => "Dispute Title&nbsp;:", "value" => urldecode($this->dispute_title)));
        $dispute_str .= $this->displayBox(array("label" => "Service Name&nbsp;:", "value" => $this->service_name));
        $dispute_str .= $this->displayBox(array("label" => "User Name&nbsp;:", "value" => $this->raise_by . " (" . $this->created_by . ")"));
        $dispute_str .= $this->displayBox(array("label" => "Raised Date&nbsp;:", "value" => $this->raise_date));
        
        $dispute_str .= "<hr>";

        $dispute_str .= "<h3>Messages : </h3>";
        $dispute_str .= urldecode($this->dispute_messages);
        $dispute_str .= "<hr>";

        if ($this->dispute_infavor > 0) {
            if ($this->customer_id == $this->dispute_infavor)
                $dispute_fvr = "Customer";
            else
                $dispute_fvr = "Provider";

            $dispute_str .= "<p style='text-align: center; line-height: 15px;'>Dispute in favor of " . $dispute_fvr . "</p>";
            $dispute_str .= "<p style='text-align: center; line-height: 0px;'>status</p><h1 style='text-align: center; line-height: 11px;'>Closed </h1>";
        }
        else {
            $dispute_str .= "<p style='text-align: center; line-height: 11px;'>status</p><h1 style='text-align: center; line-height: 11px;'>Active </h1>";
        }

        echo $dispute_str;
    }

    public function getForm() {
        $content = '';

        $main_content = new MainTemplater(DIR_ADMIN_TMPL . $this->module . "/form-nct.tpl.php");
        $main_content = $main_content->parse();

        $static_a = ($this->isActive == 'y' ? 'checked' : '');
        $static_d = ($this->isActive != 'y' ? 'checked' : '');

        $service_type_fixed = ($this->service_type == 'fixed' ? 'checked' : '');
        $service_type_hourly = ($this->service_type != 'fixed' ? 'checked' : '');

        $categories = "";
        foreach ($this->categories as $category) {
            if ($this->category_id == $category['category_id'])
                $selected = "selected";
            else
                $selected = "";

            $categories .= "<option value='" . $category['category_id'] . "' " . $selected . ">" . $category['category_name'] . "</option>";
        }

        foreach ($this->subcategories as $category) {
            if ($this->sub_category_id == $category['category_id'])
                $selected = "selected";
            else
                $selected = "";

            $subcategories .= "<option value='" . $category['category_id'] . "' " . $selected . ">" . $category['category_name'] . "</option>";
        }

        $service_img = SITE_UPD . "services/admin_service_master/" . $this->service_img;

        $fields = array(
            "%CATEGORIES%",
            "%SUB_CATEGORY%",
            "%SERVICE_NAME%",
            "%STATUS_A%",
            "%STATUS_D%",
            "%FIXED_SEVICE%",
            "%HOURLY_SERVICE%",
            "%FORM_TYPE%",
            "%ID%",
            "%CAT_IMG_SRC%"
        );

        $fields_replace = array(
            $categories,
            $subcategories,
            $this->service_name,
            $static_a,
            $static_d,
            $service_type_fixed,
            $service_type_hourly,
            $this->formtype,
            $this->service_id,
            $service_img
        );

        $content = str_replace($fields, $fields_replace, $main_content);
        return filtering($content, 'output', 'text');
    }

    public function dataGrid() {
        $content = $operation = $totalRow = NULL;
        $result = $tmp_rows = $row_data = array();

        extract($this->searchArray);
        $chr = str_replace(array('_', '%'), array('\_', '\%'), $chr);

        if (isset($chr) && $chr != '')
            $whereCond .= "  AND  (sm.service_name LIKE '%" . $chr . "%' OR d.dispute_title LIKE '%" . $chr . "%' OR u.firstName LIKE '%" . $chr . "%' OR u.lastName LIKE '%" . $chr . "%' )";

        if (isset($sort))
            $sorting = $sort . ' ' . $order;
        else
            $sorting = 'd.dispute_id DESC';

        $query = "SELECT d.dispute_id, d.dispute_infavor, d.dispute_title, d.status, d.service_id, d.createdDate,
            sm.service_name, u.firstName, u.lastName,d.createdUser, d.customer_id, d.provider_id
                    FROM tbl_dispute_master d
                    INNER JOIN tbl_users u on u.id = d.createdUser
                    INNER JOIN tbl_services s on s.id = d.service_id
                    INNER JOIN tbl_service_master sm on sm.service_id = s.service_id
                    where 1 AND s.escalate_admin = 'y'
                    " . $whereCond . " ORDER BY " . $sorting;


        $query_with_limit = $query . " LIMIT " . $offset . " ," . $rows . " ";
        $totalUsers = $this->pdoQuery($query)->results();

        $qrySel = $this->pdoQuery($query_with_limit)->results();
        $totalRow = count($totalUsers);

        foreach ($qrySel as $fetchRes) {

            $operation = '';
            $operation .= (in_array('view', $this->Permission)) ? '&nbsp;&nbsp;' . $this->operation(array("href" => "ajax." . $this->module . ".php?action=view&id=" . $fetchRes['dispute_id'] . "", "class" => "btn default blue btn-xs btn-viewbtn", "value" => '<i class="fa fa-laptop"></i>&nbsp;View')) : '';

            if ($fetchRes['dispute_infavor'] == '0') {
                $operation .= (in_array('edit', $this->Permission)) ? '&nbsp;&nbsp;' . $this->operation(array("href" => "#", "class" => "btn default btn-xs black", "extraAtt" => "onclick='return fevor_decision(0," . $fetchRes['dispute_id'] . ", " . $fetchRes['customer_id'] . " )'", "value" => '<i class="fa fa-user"></i>&nbsp;Customer Favor')) : '';
                $operation .= (in_array('edit', $this->Permission)) ? '&nbsp;&nbsp;' . $this->operation(array("href" => "#", "class" => "btn default btn-xs black", "extraAtt" => "onclick='return fevor_decision(1," . $fetchRes['dispute_id'] . ", " . $fetchRes['provider_id'] . " )'", "value" => '<i class="fa fa-users"></i>&nbsp;Provider Favor')) : '';
            }

            if ($fetchRes['customer_id'] == $fetchRes['createdUser'])
                $raised_by = $fetchRes["firstName"] . " " . $fetchRes["lastName"] . " (Customer)";
            else
                $raised_by = $fetchRes["firstName"] . " " . $fetchRes["lastName"] . " (Provider)";

            $service_id = (isset($fetchRes["dispute_id"]) && $fetchRes["dispute_id"] != '') ? $fetchRes["dispute_id"] : 'N/A';
            $dispute_title = (isset($fetchRes["dispute_title"]) && $fetchRes["dispute_title"] != '') ? $fetchRes["dispute_title"] : 'N/A';
            $service_name = (isset($fetchRes["service_name"]) && $fetchRes["service_name"] != '') ? $fetchRes["service_name"] : 'N/A';
            $category_name = (isset($fetchRes["category_name"]) && $fetchRes["category_name"] != '') ? $fetchRes["category_name"] : '';
            $subcategory_name = (isset($fetchRes["subcategory_name"]) && $fetchRes["subcategory_name"] != '') ? $fetchRes["subcategory_name"] : '';

            $service_img = "<img width='60px' height='60px' src='" . SITE_UPD . "services/admin_service_master/" . $fetchRes["service_img"] . "' />";

            if ($fetchRes['status'] == 'a')
                $status_txt = '<span class="btn red btn-xs black ">Active</span>';
            else
                $status_txt = '<span class="btn yellow btn-xs black ">Closed</span>';

            $final_array = array(
                filtering($fetchRes['dispute_id'], 'output', 'int'),
                filtering($dispute_title),
                filtering($service_name),
                filtering($raised_by),
                $status_txt
            );

            $final_array = array_merge($final_array, array($operation));
            $row_data[] = $final_array;
        }

        $result["sEcho"] = $sEcho;
        $result["iTotalRecords"] = (int) $totalRow;
        $result["iTotalDisplayRecords"] = (int) $totalRow;
        $result["aaData"] = $row_data;
        return $result;
    }

    public function displaybox($text) {

        $text['label'] = isset($text['label']) ? $text['label'] : 'Enter Text Here: ';
        $text['value'] = isset($text['value']) ? $text['value'] : '';
        $text['name'] = isset($text['name']) ? $text['name'] : '';
        $text['class'] = isset($text['class']) ? 'form-control-static ' . trim($text['class']) : 'form-control-static';
        $text['onlyField'] = isset($text['onlyField']) ? $text['onlyField'] : false;
        $text['extraAtt'] = isset($text['extraAtt']) ? $text['extraAtt'] : '';

        $main_content = new MainTemplater(DIR_ADMIN_TMPL . $this->module . '/displaybox.tpl.php');
        $main_content = $main_content->parse();
        $fields = array("%LABEL%", "%CLASS%", "%VALUE%");
        $fields_replace = array($text['label'], $text['class'], $text['value']);
        return str_replace($fields, $fields_replace, $main_content);
    }

    public function getSelectBoxOption() {
        $content = '';
        $main_content = new MainTemplater(DIR_ADMIN_TMPL . $this->module . "/select_option-nct.tpl.php");
        $content.= $main_content->parse();
        return sanitize_output($content);
    }

    public function toggel_switch($text) {
        $text['action'] = isset($text['action']) ? $text['action'] : 'Enter Action Here: ';
        $text['check'] = isset($text['check']) ? $text['check'] : '';
        $text['name'] = isset($text['name']) ? $text['name'] : '';
        $text['class'] = isset($text['class']) ? '' . trim($text['class']) : '';
        $text['extraAtt'] = isset($text['extraAtt']) ? $text['extraAtt'] : '';

        $main_content = new MainTemplater(DIR_ADMIN_TMPL . $this->module . '/switch-nct.tpl.php');
        $main_content = $main_content->parse();
        $fields = array("%NAME%", "%CLASS%", "%ACTION%", "%EXTRA%", "%CHECK%");
        $fields_replace = array($text['name'], $text['class'], $text['action'], $text['extraAtt'], $text['check']);
        return str_replace($fields, $fields_replace, $main_content);
    }

    public function operation($text) {

        $text['href'] = isset($text['href']) ? $text['href'] : 'Enter Link Here: ';
        $text['value'] = isset($text['value']) ? $text['value'] : '';
        $text['name'] = isset($text['name']) ? $text['name'] : '';
        $text['class'] = isset($text['class']) ? '' . trim($text['class']) : '';
        $text['extraAtt'] = isset($text['extraAtt']) ? $text['extraAtt'] : '';
        $main_content = new MainTemplater(DIR_ADMIN_TMPL . $this->module . '/operation-nct.tpl.php');
        $main_content = $main_content->parse();
        $fields = array("%HREF%", "%CLASS%", "%VALUE%", "%EXTRA%");
        $fields_replace = array($text['href'], $text['class'], $text['value'], $text['extraAtt']);
        return str_replace($fields, $fields_replace, $main_content);
    }

    public function getPageContent() {
        $final_result = NULL;
        $main_content = new MainTemplater(DIR_ADMIN_TMPL . $this->module . "/" . $this->module . ".tpl.php");
        $main_content->breadcrumb = $this->getBreadcrumb();

        $main_content_parsed = $final_result = $main_content->parse();

        $fields = array(
            "%VIEW_ALL_RECORDS_BTN%"
        );

        $view_all_records_btn = '';
        if (( isset($_GET['day']) && $_GET['day'] != '' ) || ( isset($_GET['month']) && $_GET['month'] != '' ) || ( isset($_GET['year']) && $_GET['year'] != '' )) {
            $view_all_records_btn = $this->getViewAllBtn();
        }

        $fields_replace = array(
            $view_all_records_btn
        );

        $final_result = str_replace($fields, $fields_replace, $main_content_parsed);

        return $final_result;
    }

    public function resolveDispute($dispute_id = 0, $user_id = 0) {
        require_once('../../../includes-nct/stripe/init.php');
        
        $sql = "SELECT d.dispute_id, d.dispute_title, d.status, d.service_id, d.createdDate
                , d.customer_id, d.provider_id, s.service_request_id,s.booking_amt
                ,sr.customer_commission,sr.provider_commission
                    FROM tbl_dispute_master d
                    INNER JOIN tbl_services s on s.id = d.service_id
                    INNER JOIN tbl_services_request sr on sr.service_request_id = s.service_request_id
                    WHERE d.dispute_id = ?";

        $where = array($dispute_id);
        $disputed_data = $this->pdoQuery($sql, $where)->result();
        //echo '<pre>';print_r($disputed_data);exit;
        $provider_id = isset($disputed_data["provider_id"]) ? $disputed_data["provider_id"] : 0;
        $service_status = ($provider_id == $user_id) ? "completed" : "closed";

        
        if($provider_id == $user_id){

            $total_booking_amount = $disputed_data['booking_amt'] - $disputed_data['booking_amt'] * ($disputed_data['provider_commission']/100);

            $account_id = getTableValue('tbl_users','stripe_user_id',array('id'=>$user_id));
            
            /* Changes 27-9-2022 start */
            /*try {
                
                $stripe = new \Stripe\StripeClient(
                  STRIPE_SECRET_ID
                );
                 
                $amount_in_cent = $total_booking_amount*100;
                $response = $stripe->transfers->create([
                              'amount' => $amount_in_cent,
                              'currency' => 'eur',
                              'destination' => $account_id,
                              'transfer_group' => 'Transfer_'.time(),
                            ]);

                $stripe_response = json_encode($response);
                $is_paid = 'y';

                $this->update('tbl_services', array("serviceStatus" => $service_status), array("service_request_id" => $disputed_data['service_request_id'])
                );

                $this->update('tbl_services_request', array("serviceStatus" => $service_status), array("service_request_id" => $disputed_data['service_request_id'])
                );

                $this->update('tbl_dispute_master', array("status" => 'd', "dispute_infavor" => $user_id, "stripe_response" => $stripe_response, "is_paid" => $is_paid), array("dispute_id" => $dispute_id)
                );

                 $data_array = array(
                    "constant" =>"AC_NT_NOTIFY_ME_WHEN_DISPUTE_CLOSE_BY_ADMIN",
                    "request_id" => $disputed_data['service_request_id'],
                    "dispute_id" => $dispute_id,
                    "created_user" => $disputed_data['provider_id'],
                );
                AddNotification($data_array);


                $data_array = array(
                    "constant" =>"AC_NT_NOTIFY_ME_WHEN_DISPUTE_CLOSED_BY_ADMIN",
                    "request_id" => $disputed_data['service_request_id'],
                    "dispute_id" => $dispute_id,
                    "created_user" => $disputed_data['customer_id'],
                );
                AddNotification($data_array);

                $response['status'] = true;
                $response['message'] = 'Dispute solved successfully';
            }
            catch(Exception $e) {
              $stripe_response = $e->getMessage();
              $is_paid = 'n';

              $this->update('tbl_dispute_master', array("stripe_response" => $stripe_response), array("dispute_id" => $dispute_id)
                );

              $response['status'] = false;
              $response['message'] = $stripe_response;

            }*/
            
            $is_paid = 'y';

            $this->update('tbl_services', array("serviceStatus" => $service_status), array("service_request_id" => $disputed_data['service_request_id'])
            );

            $this->update('tbl_services_request', array("serviceStatus" => $service_status), array("service_request_id" => $disputed_data['service_request_id'])
            );

            $this->update('tbl_dispute_master', array("status" => 'd', "dispute_infavor" => $user_id, "is_paid" => $is_paid), array("dispute_id" => $dispute_id)
            );

             $data_array = array(
                "constant" =>"AC_NT_NOTIFY_ME_WHEN_DISPUTE_CLOSE_BY_ADMIN",
                "request_id" => $disputed_data['service_request_id'],
                "dispute_id" => $dispute_id,
                "created_user" => $disputed_data['provider_id'],
            );
            AddNotification($data_array);


            $data_array = array(
                "constant" =>"AC_NT_NOTIFY_ME_WHEN_DISPUTE_CLOSED_BY_ADMIN",
                "request_id" => $disputed_data['service_request_id'],
                "dispute_id" => $dispute_id,
                "created_user" => $disputed_data['customer_id'],
            );
            AddNotification($data_array);

            $response['status'] = true;
            $response['message'] = 'Dispute solved successfully';
            /* Changes 27-9-2022 end */


        }else{
            //echo $disputed_data['service_request_id'];exit;
            $total_booking_amount = $disputed_data['booking_amt'];
            $payment = $this->db->select('tbl_payment_master',array('payment_instant_id'),array('service_request_id'=>$disputed_data['service_request_id'],'customer_id'=>$disputed_data['customer_id'],'provider_id'=>$disputed_data['provider_id']))->result();
            try {

                $stripe = new \Stripe\StripeClient(
                  STRIPE_SECRET_ID
                );
                //$amount_in_cent = $total_booking_amount*100;
                /* Changes 27-9-2022 start */
                $stripe_response = $stripe->refunds->create([
                    'payment_intent' => $payment['payment_instant_id'],
                    'reverse_transfer' => true,
                    'refund_application_fee' => true
                ]);
                /* Changes 27-9-2022 end */

                $stripe_response = json_encode($response);
                $is_paid = 'y';

                $this->update('tbl_services', array("serviceStatus" => $service_status), array("service_request_id" => $disputed_data['service_request_id'])
                );

                $this->update('tbl_services_request', array("serviceStatus" => $service_status), array("service_request_id" => $disputed_data['service_request_id'])
                );

                $this->update('tbl_dispute_master', array("status" => 'd', "dispute_infavor" => $user_id, "stripe_response" => $stripe_response, "is_paid" => $is_paid), array("dispute_id" => $dispute_id)
                );

                 $data_array = array(
                    "constant" =>"AC_NT_NOTIFY_ME_WHEN_DISPUTE_CLOSE_BY_ADMIN",
                    "request_id" => $disputed_data['service_request_id'],
                    "dispute_id" => $dispute_id,
                    "created_user" => $disputed_data['provider_id'],
                );
                AddNotification($data_array);


                $data_array = array(
                    "constant" =>"AC_NT_NOTIFY_ME_WHEN_DISPUTE_CLOSED_BY_ADMIN",
                    "request_id" => $disputed_data['service_request_id'],
                    "dispute_id" => $dispute_id,
                    "created_user" => $disputed_data['customer_id'],
                );
                AddNotification($data_array);

                $response['status'] = true;
                $response['message'] = 'Dispute solved successfully';
            }
            catch(Exception $e) {
              $stripe_response = $e->getMessage();
              $is_paid = 'n';

              $this->update('tbl_dispute_master', array("stripe_response" => $stripe_response), array("dispute_id" => $dispute_id)
                );

              $response['status'] = false;
              $response['message'] = $stripe_response;

            }
        }


        //$this->pdoQuery("UPDATE tbl_users SET wallet = wallet+" . $total_booking_amount . " WHERE id = ? ", array($user_id));

        return $response;
    }

}
